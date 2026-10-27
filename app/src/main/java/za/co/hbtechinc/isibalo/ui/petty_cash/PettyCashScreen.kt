package za.co.hbtechinc.isibalo.ui.petty_cash

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
import androidx.compose.material.icons.filled.Check
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
                Column(Modifier.padding(16.dp)) {
                    Text("Unsettled Petty Cash Issued", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text(unsettledTotal.rands(), color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("${records.count { !it.settled }} active petty cash vouchers", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        item {
            SquareButton("ISSUE PETTY CASH", { issuing = true }, Modifier.fillMaxWidth())
        }
        if (records.isEmpty()) {
            item {
                Text("No petty cash vouchers recorded yet. Track cash advances here.", color = cs.onSurfaceVariant)
            }
        }
        items(records, key = { it.id }) { record ->
            PettyCashItem(
                record = record,
                onSettle = { vm.settle(record.id) },
                onDelete = { vm.delete(record) }
            )
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

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text("Issue Petty Cash") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = recipient, onValueChange = { recipient = it }, label = { Text("Recipient / Staff Name") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it }, label = { Text("Amount Issued (R)") },
                    singleLine = true, shape = RectangleShape, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = note, onValueChange = { note = it }, label = { Text("Reason / Purpose (e.g. Change, Transport)") },
                    shape = RectangleShape, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            SquareButton("ISSUE", {
                if (recipient.isNotBlank() && amount.isNotBlank()) {
                    onSave(recipient, amount, note)
                }
            })
        },
        dismissButton = { SquareButton("CANCEL", onDismiss, outlined = true) }
    )
}
