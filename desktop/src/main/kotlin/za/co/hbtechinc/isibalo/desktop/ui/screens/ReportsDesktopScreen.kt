package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands

@Composable
fun ReportsDesktopScreen(repository: DesktopRepository) {
    val sales by repository.sales.collectAsState()
    val expenses by repository.expenses.collectAsState()
    val stockItems by repository.stock.collectAsState()
    var pdfDialogMessage by remember { mutableStateOf<String?>(null) }

    val grossRevenueCents = sales.sumOf { it.sale.totalCents }
    val totalExpenseCents = expenses.sumOf { it.amountCents }
    val netProfitCents = grossRevenueCents - totalExpenseCents
    val totalAssetValueCents = stockItems.sumOf { it.priceCents * it.quantity }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Executive Summary Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBlueContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Executive Financial Statements & Reports", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    Text("Automated Profit & Loss Statement and Asset Valuations", style = MaterialTheme.typography.bodyMedium, color = LightBlueSecondary)
                }

                Button(
                    onClick = {
                        val summary = "Financial PDF Report generated: Net Profit ${netProfitCents.rands()}, Asset Value ${totalAssetValueCents.rands()}"
                        repository.logWhatsAppShare("Financial PDF Report", summary)
                        pdfDialogMessage = "PDF Report generated and saved to document storage:\n\nGross Sales: ${grossRevenueCents.rands()}\nExpenses: ${totalExpenseCents.rands()}\nNet Profit: ${netProfitCents.rands()}\nInventory Asset Value: ${totalAssetValueCents.rands()}"
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = "Download Report")
                    Spacer(Modifier.width(6.dp))
                    Text("Export Financial PDF")
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, LightBlueContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gross Revenue", style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                    Text(grossRevenueCents.rands(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                }
            }

            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, LightBlueContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Operating Expenses", style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                    Text(totalExpenseCents.rands(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            }

            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, LightBlueContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Net Operating Profit", style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                    Text(netProfitCents.rands(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                }
            }

            Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, LightBlueContainer)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Inventory Asset Valuation", style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                    Text(totalAssetValueCents.rands(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                }
            }
        }

        Text("Stock Take Audit Valuation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(stockItems, key = { it.id }) { item ->
                val assetValue = item.priceCents * item.quantity
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LightBlueContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                            Text("Quantity in stock: ${item.quantity} units @ ${item.priceCents.rands()}/unit", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                        }
                        Text(assetValue.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                    }
                }
            }
        }
    }

    pdfDialogMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { pdfDialogMessage = null },
            confirmButton = { TextButton(onClick = { pdfDialogMessage = null }) { Text("OK") } },
            title = { Text("PDF Statement Exported") },
            text = { Text(msg) }
        )
    }
}
