package za.co.hbtechinc.isibalo.ui.credit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.CustomerDebt
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands

@Composable
fun CreditScreen(vm: CreditViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val debts by vm.debts.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val totalOutstanding = debts.filter { !it.settled }.sumOf { it.amountCents }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Customer Credit Tabs (Isikwelete)", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Total Outstanding: ${totalOutstanding.rands()}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Track customer credit balances and tab settlements", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareButton("RECORD CUSTOMER TAB", { adding = true }, Modifier.fillMaxWidth())
        }

        if (debts.isEmpty()) {
            item {
                Text("No customer credit tabs recorded yet.", color = cs.onSurfaceVariant)
            }
        }

        items(debts, key = { it.id }) { debt ->
            DebtItem(debt = debt, onSettle = { vm.settle(debt.id) }, onDelete = { vm.delete(debt) })
        }
    }

    if (adding) {
        AddDebtDialog(
            onDismiss = { adding = false },
            onSave = { name, phone, amount, note ->
                vm.addDebt(name, phone, amount, note)
                adding = false
            }
        )
    }
}

@Composable
fun DebtItem(debt: CustomerDebt, onSettle: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    SquareCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(debt.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (debt.phone.isNotBlank()) Text("Phone: ${debt.phone}", style = MaterialTheme.typography.labelSmall)
                if (debt.note.isNotBlank()) Text("Note: ${debt.note}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Text(
                    text = if (debt.settled) "SETTLED" else "OWES: ${debt.amountCents.rands()}",
                    fontWeight = FontWeight.Bold,
                    color = if (debt.settled) cs.primary else cs.error
                )
            }
            Row {
                if (!debt.settled) {
                    IconButton(onClick = onSettle) {
                        Icon(Icons.Filled.Check, contentDescription = "Settle Tab", tint = cs.primary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Record", tint = cs.error)
                }
            }
        }
    }
}

@Composable
private fun AddDebtDialog(onDismiss: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Record Customer Tab (Isikwelete)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it }, label = { Text("Customer Name") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, label = { Text("Amount Owed (R)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = note, onValueChange = { note = it }, label = { Text("Items / Note (e.g. 2x Bread, 1x Milk)") },
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SquareButton("CANCEL", onDismiss, Modifier.weight(1f), outlined = true)
                    SquareButton("SAVE", { if (name.isNotBlank() && amount.isNotBlank()) onSave(name, phone, amount, note) }, Modifier.weight(1f))
                }
            }
        }
    }
}
