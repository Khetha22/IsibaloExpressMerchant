package za.co.hbtechinc.isibalo.ui.cashup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.CashUpRecord
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashUpScreen(vm: CashUpViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val history by vm.cashUpHistory.collectAsStateWithLifecycle()
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "End of Day Cash-Up",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onBackground
            )
            Text(
                text = "Reconcile cash in drawer against system sales.",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant
            )
        }

        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Today's Expected System Totals", fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expected Cash Sales:")
                        Text(state.expectedCashCents.rands(), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Card Sales:")
                        Text(state.cardTotalCents.rands())
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mobile / Speedpoint:")
                        Text(state.mobileTotalCents.rands())
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cash Reconciliation", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = state.floatText,
                        onValueChange = vm::onFloatChanged,
                        label = { Text("Opening Till Float (R)") },
                        singleLine = true,
                        shape = RectangleShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.actualCashText,
                        onValueChange = vm::onActualCashChanged,
                        label = { Text("Actual Cash Counted in Drawer (R)") },
                        singleLine = true,
                        shape = RectangleShape,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val variance = state.varianceCents
                    val varianceColor = when {
                        variance == 0L -> Color(0xFF2E7D32)
                        variance > 0L -> Color(0xFF1565C0)
                        else -> Color(0xFFC62828)
                    }
                    val varianceStatus = when {
                        variance == 0L -> "Balanced Exact"
                        variance > 0L -> "Over by ${variance.rands()}"
                        else -> "Short by ${(-variance).rands()}"
                    }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(varianceColor.copy(alpha = 0.1f))
                            .padding(12.dp)
                    ) {
                        Text("Calculated Variance", style = MaterialTheme.typography.labelLarge)
                        Text(
                            varianceStatus,
                            style = MaterialTheme.typography.titleMedium,
                            color = varianceColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedTextField(
                        value = state.notesText,
                        onValueChange = vm::onNotesChanged,
                        label = { Text("Shift Notes / Explanation") },
                        shape = RectangleShape,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SquareButton(
                        text = if (state.isSavedSuccess) "CASH-UP RECORDED!" else "SAVE CASH-UP RECORD",
                        onClick = vm::saveCashUp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.actualCashText.isNotBlank()
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Cash-Up History Log", style = MaterialTheme.typography.titleMedium)
        }

        if (history.isEmpty()) {
            item {
                Text(
                    "No previous cash-up records saved yet.",
                    color = cs.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(history) { record ->
                CashUpRecordItem(record)
            }
        }
    }
}

@Composable
fun CashUpRecordItem(record: CashUpRecord) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT).format(Date(record.createdAt))
    val cs = MaterialTheme.colorScheme

    SquareCard(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateStr, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val statusText = when {
                    record.varianceCents == 0L -> "Balanced"
                    record.varianceCents > 0L -> "+${record.varianceCents.rands()}"
                    else -> "-${(-record.varianceCents).rands()}"
                }
                Text(statusText, fontWeight = FontWeight.Bold, color = if (record.varianceCents < 0) cs.error else cs.primary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expected Cash: ${record.expectedCashCents.rands()}", style = MaterialTheme.typography.bodyMedium)
                Text("Actual Cash: ${record.actualCashCents.rands()}", style = MaterialTheme.typography.bodyMedium)
            }
            if (record.notes.isNotBlank()) {
                Text("Notes: ${record.notes}", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
        }
    }
}
