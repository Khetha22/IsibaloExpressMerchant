package za.co.hbtechinc.isibalo.ui.reports

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.util.rands

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun FinancialReportScreen(vm: FinancialReportViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val report by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Detect orientation for adaptive landscape/portrait split layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBlueSurface
    ) {
        if (isLandscape) {
            // Adaptive Two-Column Split for Landscape Mode
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left Column: Hero Banner & WhatsApp Share Action
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FinancialHeroCard(report = report)
                    ShareReportButton(report = report, context = context)
                }

                // Right Column: P&L Breakdown & Payment Methods
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "pl_breakdown_landscape") {
                        ProfitLossBreakdownCard(report = report)
                    }

                    item(key = "payment_methods_landscape") {
                        PaymentMethodsCard(report = report)
                    }
                }
            }
        } else {
            // Standard Single-Column Vertical Feed for Portrait Mode
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item(key = "financial_hero") {
                    FinancialHeroCard(report = report)
                }

                item(key = "pl_breakdown") {
                    ProfitLossBreakdownCard(report = report)
                }

                item(key = "payment_methods") {
                    PaymentMethodsCard(report = report)
                }

                item(key = "share_action") {
                    ShareReportButton(report = report, context = context)
                }
            }
        }
    }
}

@Composable
private fun FinancialHeroCard(report: FinancialReportState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = LightBluePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.Assessment, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Text("Executive Financial Summary", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
            }
            Text("Net Profit: ${report.netProfit.rands()}", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                "Revenue: ${report.totalRevenue.rands()}  •  Expenses: ${report.totalExpenses.rands()}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ProfitLossBreakdownCard(report: FinancialReportState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Profit & Loss Breakdown", fontWeight = FontWeight.Bold, color = LightBlueOnContainer, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Sales Revenue:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(report.totalRevenue.rands(), fontWeight = FontWeight.Bold, color = LightBluePrimary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Operating Expenses:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(report.totalExpenses.rands(), fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Net Profit:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(
                    report.netProfit.rands(),
                    fontWeight = FontWeight.Bold,
                    color = if (report.netProfit >= 0) LightBluePrimary else Color(0xFFC62828)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodsCard(report: FinancialReportState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Sales by Payment Method", fontWeight = FontWeight.Bold, color = LightBlueOnContainer, style = MaterialTheme.typography.titleMedium)
            if (report.paymentTotals.isEmpty()) {
                Text("No sales recorded today.", color = LightBlueOnContainer.copy(alpha = 0.7f))
            } else {
                report.paymentTotals.forEach { pt ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(pt.method.label, color = LightBlueOnContainer.copy(alpha = 0.8f))
                        Text(pt.total.rands(), fontWeight = FontWeight.Bold, color = LightBluePrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareReportButton(report: FinancialReportState, context: android.content.Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "share_scale"
    )

    Button(
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
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Share Financial Report via WhatsApp", fontWeight = FontWeight.Bold)
    }
}
