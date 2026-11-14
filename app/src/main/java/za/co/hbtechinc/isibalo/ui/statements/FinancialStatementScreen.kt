package za.co.hbtechinc.isibalo.ui.statements

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
import za.co.hbtechinc.isibalo.util.PdfReportGenerator
import za.co.hbtechinc.isibalo.util.rands

@Composable
fun FinancialStatementScreen(vm: FinancialStatementViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
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
                    Text("Official Financial Statements & PDF", color = cs.onPrimary, style = MaterialTheme.typography.labelLarge)
                    Text("Net Income: ${state.netProfit.rands()}", color = cs.onPrimary, style = MaterialTheme.typography.headlineMedium)
                    Text("Total Assets: ${state.totalAssetBase.rands()}  •  Stock Value: ${state.stockValuation.rands()}", color = cs.onPrimary.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            SquareButton(
                text = "📄 GENERATE & SHARE PDF FINANCIAL STATEMENT",
                onClick = {
                    val pdfFile = PdfReportGenerator.generateFinancialStatementPdf(
                        context = context,
                        totalRevenueCents = state.totalRevenue,
                        totalExpensesCents = state.totalExpenses,
                        netProfitCents = state.netProfit,
                        stockValuationCents = state.stockValuation,
                        outstandingTabsCents = state.outstandingTabs,
                        paymentTotals = state.paymentTotals
                    )
                    if (pdfFile != null) {
                        PdfReportGenerator.sharePdf(context, pdfFile)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Statement of Profit & Loss (P&L)", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Sales Revenue:")
                        Text(state.totalRevenue.rands(), fontWeight = FontWeight.Bold, color = cs.primary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Operating Expenses:")
                        Text("-${state.totalExpenses.rands()}", fontWeight = FontWeight.Bold, color = cs.error)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Profit / Income:", fontWeight = FontWeight.Bold)
                        Text(state.netProfit.rands(), fontWeight = FontWeight.Bold, color = if (state.netProfit >= 0) cs.primary else cs.error)
                    }
                }
            }
        }

        item {
            SquareCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Balance Sheet Summary", fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Inventory Assets (Retail Value):")
                        Text(state.stockValuation.rands(), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Outstanding Customer Tabs (*Isikwelete*):")
                        Text(state.outstandingTabs.rands(), fontWeight = FontWeight.Bold, color = cs.error)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Estimated Asset Base:", fontWeight = FontWeight.Bold)
                        Text(state.totalAssetBase.rands(), fontWeight = FontWeight.Bold, color = cs.primary)
                    }
                }
            }
        }
    }
}
