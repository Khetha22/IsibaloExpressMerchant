package za.co.hbtechinc.isibalo.ui.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.util.rands

@Composable
fun StockTakeScreen(vm: StockTakeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val report by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primaryContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Stock Take & Inventory Audit", color = cs.onPrimaryContainer, style = MaterialTheme.typography.labelLarge)
                    Text("Retail Valuation: ${report.totalRetailValueCents.rands()}", color = cs.onPrimaryContainer, style = MaterialTheme.typography.headlineMedium)
                    Text("Total Items: ${report.totalUnits} units (${report.items.size} SKUs)  •  Cost Value: ${report.totalCostValueCents.rands()}", color = cs.onPrimaryContainer.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Inventory Health Audit", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Low Stock Items:")
                        Text("${report.lowStockCount} SKUs", fontWeight = FontWeight.Bold, color = if (report.lowStockCount > 0) cs.error else cs.primary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Expired / Expiring Soon:")
                        Text("${report.expiredCount} SKUs", fontWeight = FontWeight.Bold, color = if (report.expiredCount > 0) cs.error else cs.primary)
                    }
                }
            }
        }

        item {
            SquareButton(
                text = "SHARE STOCK TAKE AUDIT VIA WHATSAPP",
                onClick = {
                    val reportText = buildString {
                        appendLine("📦 *Isibalo Express Stock Take Audit*")
                        appendLine("Total SKUs: ${report.items.size}")
                        appendLine("Total Units: ${report.totalUnits}")
                        appendLine("Stock Value (Retail): ${report.totalRetailValueCents.rands()}")
                        appendLine("Stock Value (Cost): ${report.totalCostValueCents.rands()}")
                        appendLine("Low Stock SKUs: ${report.lowStockCount}")
                    }
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(reportText)}")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {}
                },
                modifier = Modifier.fillMaxWidth(),
                outlined = true
            )
        }

        item {
            Text("Complete Inventory Valuation List", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
        }

        items(report.items, key = { it.id }) { item ->
            SquareCard(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Qty: ${item.quantity} units  •  Cost: ${item.costCents.rands()}  •  Price: ${item.priceCents.rands()}", style = MaterialTheme.typography.labelSmall)
                        Text("Total Value: ${(item.priceCents * item.quantity).rands()}", style = MaterialTheme.typography.bodyMedium, color = cs.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
