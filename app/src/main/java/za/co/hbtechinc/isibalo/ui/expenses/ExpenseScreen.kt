package za.co.hbtechinc.isibalo.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
            SquareCard(Modifier.fillMaxWidth(), color = cs.errorContainer) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Business Expenses", color = cs.onErrorContainer, style = MaterialTheme.typography.labelLarge)
                    Text(totalExpenses.rands(), color = cs.onErrorContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("${expenses.size} recorded expenses", color = cs.onErrorContainer, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            SquareButton("RECORD NEW EXPENSE", { adding = true }, Modifier.fillMaxWidth())
        }
        if (expenses.isEmpty()) {
            item {
                Text("No expenses recorded yet. Track rent, transport, and stock costs here.", color = cs.onSurfaceVariant)
            }
        }
        items(expenses, key = { it.id }) { expense ->
            ExpenseItem(expense = expense, onDelete = { vm.delete(expense) })
        }
    }

    if (adding) {
        AddExpenseDialog(
            onDismiss = { adding = false },
            onSave = { title, amount, cat ->
                vm.addExpense(title, amount, cat)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text("Record Business Expense") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it }, label = { Text("Expense Title (e.g. Transport / Electricity)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it }, label = { Text("Amount (R)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Utilities, Stock, Wages)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SquareButton("SAVE", {
                if (title.isNotBlank() && amount.isNotBlank()) {
                    onSave(title, amount, category)
                }
            })
        },
        dismissButton = { SquareButton("CANCEL", onDismiss, outlined = true) }
    )
}
