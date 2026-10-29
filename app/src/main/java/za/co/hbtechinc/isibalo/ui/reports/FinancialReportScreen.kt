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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun FinancialReportScreen(vm: FinancialReportViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val report by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            SquareCard(Modifier.fillMaxWidth(), color = cs.primary) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Executive Financial Summary", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text("Net Profit: ${report.netProfit.rands()}", color = cs.onPrimary, style = MaterialTheme.typography.headlineMedium)
                    Text("Revenue: ${report.totalRevenue.rands()}  •  Expenses: ${report.totalExpenses.rands()}", color = cs.onPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Profit & Loss Breakdown", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Sales Revenue:")
                        Text(report.totalRevenue.rands(), fontWeight = FontWeight.Bold, color = cs.primary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Operating Expenses:")
                        Text(report.totalExpenses.rands(), fontWeight = FontWeight.Bold, color = cs.error)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Profit:")
                        Text(report.netProfit.rands(), fontWeight = FontWeight.Bold, color = if (report.netProfit >= 0) cs.primary else cs.error)
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Sales by Payment Method", fontWeight = FontWeight.Bold)
                    if (report.paymentTotals.isEmpty()) {
                        Text("No sales recorded today.", color = cs.onSurfaceVariant)
                    } else {
                        report.paymentTotals.forEach { pt ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(pt.method.label)
                                Text(pt.total.rands(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            SquareButton(
                text = "SHARE FINANCIAL REPORT VIA WHATSAPP",
                onClick = {
                    val reportText = buildString {
                        appendLine("📊 *Isibalo Express Financial Report*")
                        appendLine("Revenue: ${report.totalRevenue.rands()}")
                        appendLine("Expenses: ${report.totalExpenses.rands()}")
                        appendLine("Net Profit: ${report.netProfit.rands()}")
                        appendLine("Status: Fully Audited & Ready for FNB Loan Review")
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
    }
}
