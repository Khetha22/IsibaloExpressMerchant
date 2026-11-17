package za.co.hbtechinc.isibalo.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
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
import za.co.hbtechinc.isibalo.util.PdfReportGenerator
import za.co.hbtechinc.isibalo.util.rands

@Composable
fun OwnerDashboardScreen(vm: OwnerDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primary) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Owner Live Business Dashboard", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text(state.totalRevenue.rands(), color = cs.onPrimary, style = MaterialTheme.typography.headlineMedium)
                    Text("Today's Net Profit: ${state.netProfit.rands()}  •  ${state.todaySales.size} Transactions", color = cs.onPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SquareCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Active SKUs", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        Text("${state.stockItems.size}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = cs.primary)
                    }
                }
                SquareCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Low Stock", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        Text("${state.lowStockCount}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = if (state.lowStockCount > 0) cs.error else cs.primary)
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Executive Quick Actions", fontWeight = FontWeight.Bold, color = cs.primary)
                    SquareButton(
                        text = "📄 GENERATE & SHARE AUDITED PDF STATEMENT",
                        onClick = {
                            val pdfFile = PdfReportGenerator.generateFinancialStatementPdf(
                                context = context,
                                totalRevenueCents = state.totalRevenue,
                                totalExpensesCents = state.todayExpenses,
                                netProfitCents = state.netProfit,
                                stockValuationCents = state.stockItems.sumOf { it.priceCents * it.quantity },
                                outstandingTabsCents = 0L,
                                paymentTotals = state.paymentTotals
                            )
                            if (pdfFile != null) {
                                PdfReportGenerator.sharePdf(context, pdfFile, "Owner Dashboard Financial Statement")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        outlined = true
                    )
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CloudSync, null, tint = cs.primary, modifier = Modifier.padding(end = 12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Cloud Backend Synchronization", fontWeight = FontWeight.Bold)
                        Text(
                            if (state.pendingSyncCount == 0) "All records synced with hbtechinc.co.za" else "${state.pendingSyncCount} records pending sync",
                            style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                        )
                    }
                    SquareButton("SYNC NOW", vm::syncNow)
                }
            }
        }

        if (state.lowStockCount > 0 || state.expiredCount > 0) {
            item {
                SquareCard(Modifier.fillMaxWidth(), color = cs.errorContainer) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = cs.onErrorContainer, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text("Inventory Health Alerts", fontWeight = FontWeight.Bold, color = cs.onErrorContainer)
                            Text("${state.lowStockCount} items low in stock  •  ${state.expiredCount} items expiring/expired", color = cs.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sales by Payment Method", fontWeight = FontWeight.Bold)
                    if (state.paymentTotals.isEmpty()) {
                        Text("No sales recorded today.", color = cs.onSurfaceVariant)
                    } else {
                        state.paymentTotals.forEach { pt ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(pt.method.label)
                                Text(pt.total.rands(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
