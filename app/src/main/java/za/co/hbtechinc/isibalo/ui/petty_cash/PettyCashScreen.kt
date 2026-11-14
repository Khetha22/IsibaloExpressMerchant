package za.co.hbtechinc.isibalo.ui.petty_cash

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
import za.co.hbtechinc.isibalo.data.PettyCash
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PettyCashScreen(vm: PettyCashViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val records by vm.records.collectAsStateWithLifecycle()
    var issuing by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val unsettledTotal = records.filter { !it.settled }.sumOf { it.amountCents }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Petty Cash Management", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Unsettled Vouchers: ${unsettledTotal.rands()}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Issued vouchers and staff expense advances", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareButton("ISSUE PETTY CASH VOUCHER", { issuing = true }, Modifier.fillMaxWidth())
        }

        if (records.isEmpty()) {
            item {
                Text("No petty cash vouchers issued yet.", color = cs.onSurfaceVariant)
            }
        }

        items(records, key = { it.id }) { record ->
            PettyCashItem(record = record, onSettle = { vm.settle(record.id) }, onDelete = { vm.delete(record) })
        }
    }

    if (issuing) {
        IssuePettyCashDialog(
            onDismiss = { issuing = false },
            onSave = { recipient, amount, note ->
                vm.issuePettyCash(recipient, amount, note)
                issuing = false
            }
        )
    }
}

@Composable
fun PettyCashItem(record: PettyCash, onSettle: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT).format(Date(record.issuedAt))

    SquareCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Issued to: ${record.recipient}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (record.note.isNotBlank()) Text("Reason: ${record.note}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                Text("Date: $dateStr", style = MaterialTheme.typography.labelSmall)
                Text(
                    text = if (record.settled) "SETTLED / RETURNED" else "ISSUED: ${record.amountCents.rands()}",
                    fontWeight = FontWeight.Bold,
                    color = if (record.settled) cs.primary else cs.error
                )
            }
            Row {
                if (!record.settled) {
                    IconButton(onClick = onSettle) {
                        Icon(Icons.Filled.Check, contentDescription = "Settle Voucher", tint = cs.primary)
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
private fun IssuePettyCashDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var recipient by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Issue Petty Cash", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = recipient, onValueChange = { recipient = it }, label = { Text("Recipient / Staff Name") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, label = { Text("Amount Issued (R)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = note, onValueChange = { note = it }, label = { Text("Reason / Purpose (e.g. Change, Transport)") },
                        shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SquareButton("CANCEL", onDismiss, Modifier.weight(1f), outlined = true)
                    SquareButton("ISSUE", { if (recipient.isNotBlank() && amount.isNotBlank()) onSave(recipient, amount, note) }, Modifier.weight(1f))
                }
            }
        }
    }
}
