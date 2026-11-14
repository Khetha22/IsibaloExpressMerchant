package za.co.hbtechinc.isibalo.ui.expenses

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
import za.co.hbtechinc.isibalo.data.Expense
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpenseScreen(vm: ExpenseViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val expenses by vm.expenses.collectAsStateWithLifecycle()
    var adding by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val totalExpenses = expenses.sumOf { it.amountCents }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Business Expenses & Operating Costs", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Total Expenses: ${totalExpenses.rands()}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Track rent, transport, electricity, and stock costs", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareButton("RECORD EXPENSE", { adding = true }, Modifier.fillMaxWidth())
        }

        if (expenses.isEmpty()) {
            item {
                Text("No business expenses recorded yet.", color = cs.onSurfaceVariant)
            }
        }

        items(expenses, key = { it.id }) { expense ->
            ExpenseItem(expense = expense, onDelete = { vm.delete(expense) })
        }
    }

    if (adding) {
        AddExpenseDialog(
            onDismiss = { adding = false },
            onSave = { title, amount, category ->
                vm.addExpense(title, amount, category)
                adding = false
            }
        )
    }
}

@Composable
fun ExpenseItem(expense: Expense, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT).format(Date(expense.createdAt))

    SquareCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Category: ${expense.category}  |  $dateStr", style = MaterialTheme.typography.labelSmall)
                Text(expense.amountCents.rands(), fontWeight = FontWeight.Bold, color = cs.error)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete Expense", tint = cs.error)
            }
        }
    }
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Record Business Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it }, label = { Text("Expense Title (e.g. Transport / Electricity)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it }, label = { Text("Amount (R)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Utilities, Stock, Wages)") },
                        singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SquareButton("CANCEL", onDismiss, Modifier.weight(1f), outlined = true)
                    SquareButton("SAVE", { if (title.isNotBlank() && amount.isNotBlank()) onSave(title, amount, category) }, Modifier.weight(1f))
                }
            }
        }
    }
}
