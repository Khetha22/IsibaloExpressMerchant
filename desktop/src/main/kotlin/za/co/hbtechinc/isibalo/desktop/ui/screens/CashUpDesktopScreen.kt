package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import za.co.hbtechinc.isibalo.desktop.data.CashUpRecord
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.data.PaymentMethod
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.toCents
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashUpDesktopScreen(repository: DesktopRepository) {
    val sales by repository.sales.collectAsState()
    val cashUpHistory by repository.cashUp.collectAsState()
    val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.ROOT)

    var floatText by remember { mutableStateOf("500.00") }
    var actualCashText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var savedMessage by remember { mutableStateOf<String?>(null) }

    val todayCashSalesCents = remember(sales) {
        sales.filter { it.sale.method == PaymentMethod.CASH }.sumOf { it.sale.totalCents }
    }
    val todayCardSalesCents = remember(sales) {
        sales.filter { it.sale.method == PaymentMethod.CARD }.sumOf { it.sale.totalCents }
    }
    val todayMobileSalesCents = remember(sales) {
        sales.filter { it.sale.method == PaymentMethod.MOBILE || it.sale.method == PaymentMethod.QR_CODE }.sumOf { it.sale.totalCents }
    }

    val floatCents = floatText.toCents() ?: 0L
    val expectedCashCents = floatCents + todayCashSalesCents
    val actualCashCents = actualCashText.toCents() ?: 0L
    val varianceCents = actualCashCents - expectedCashCents

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: Inputs & Calculations
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, LightBlueContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Cash-Up Drawer Reconciliation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = LightBlueSurface)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Expected Drawer Cash", style = MaterialTheme.typography.labelSmall, color = LightBlueSecondary)
                                Text(expectedCashCents.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                            }
                        }
                        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = LightBlueSurface)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Today's Cash Sales", style = MaterialTheme.typography.labelSmall, color = LightBlueSecondary)
                                Text(todayCashSalesCents.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                            }
                        }
                    }

                    SquareField(value = floatText, onValueChange = { floatText = it }, label = "Float (Opening Cash in Till)", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    SquareField(value = actualCashText, onValueChange = { actualCashText = it }, label = "Actual Physical Counted Cash", keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    SquareField(value = notesText, onValueChange = { notesText = it }, label = "Shift Notes / Variance Explanation")

                    // Variance Status
                    val varianceColor = when {
                        varianceCents == 0L -> LightBluePrimary
                        varianceCents > 0L -> Color(0xFF2E7D32)
                        else -> MaterialTheme.colorScheme.error
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = LightBlueSurface)) {
                        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Drawer Variance", style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                                Text(varianceCents.rands(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = varianceColor)
                            }
                            Surface(shape = RoundedCornerShape(10.dp), color = varianceColor.copy(alpha = 0.15f)) {
                                Text(
                                    when {
                                        varianceCents == 0L -> "Balanced"
                                        varianceCents > 0L -> "Surplus"
                                        else -> "Shortage"
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = varianceColor
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val record = CashUpRecord(
                                floatCents = floatCents,
                                expectedCashCents = expectedCashCents,
                                actualCashCents = actualCashCents,
                                varianceCents = varianceCents,
                                cardTotalCents = todayCardSalesCents,
                                mobileTotalCents = todayMobileSalesCents,
                                notes = notesText
                            )
                            repository.saveCashUp(record)
                            actualCashText = ""
                            notesText = ""
                            savedMessage = "Cash-up record successfully saved!"
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Save")
                        Spacer(Modifier.width(8.dp))
                        Text("Save & Close Cash-Up")
                    }
                }
            }
        }

        // Right Column: Cash-Up Log History
        Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Cash-Up Audit History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cashUpHistory, key = { it.id }) { record ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, LightBlueContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(fmt.format(Date(record.createdAt)), fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                Text("Variance: ${record.varianceCents.rands()}", fontWeight = FontWeight.Bold, color = if (record.varianceCents == 0L) LightBluePrimary else MaterialTheme.colorScheme.error)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Float: ${record.floatCents.rands()}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                Text("Expected: ${record.expectedCashCents.rands()}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                Text("Actual: ${record.actualCashCents.rands()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                            }
                            if (record.notes.isNotBlank()) {
                                Text("Notes: ${record.notes}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    savedMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { savedMessage = null },
            confirmButton = { TextButton(onClick = { savedMessage = null }) { Text("OK") } },
            title = { Text("Cash-Up Saved") },
            text = { Text(msg) }
        )
    }
}
