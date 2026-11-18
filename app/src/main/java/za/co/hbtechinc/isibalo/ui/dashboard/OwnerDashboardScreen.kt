package za.co.hbtechinc.isibalo.ui.dashboard

import android.content.res.Configuration
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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Warning
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
import za.co.hbtechinc.isibalo.util.PdfReportGenerator
import za.co.hbtechinc.isibalo.util.rands

// Consistent Light Blue palette matching Assistant, Dashboard, Jobs, Prices, Reports, Settings, AI Setup, and Cash-Up screens
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun OwnerDashboardScreen(vm: OwnerDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
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
                // Left Column: Live Revenue Banner & Executive Actions
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OwnerLiveHeroCard(state = state)
                    ExecutiveQuickActionsCard(state = state, context = context)
                    SyncCard(state = state, vm = vm)
                }

                // Right Column: SKUs, Low Stock Alerts, and Payment Breakdown Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "stats_row_landscape") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            StatCard(
                                title = "Active SKUs",
                                value = "${state.stockItems.size}",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Low Stock",
                                value = "${state.lowStockCount}",
                                valueColor = if (state.lowStockCount > 0) Color(0xFFC62828) else LightBluePrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (state.lowStockCount > 0 || state.expiredCount > 0) {
                        item(key = "alerts_card_landscape") {
                            AlertsCard(state = state)
                        }
                    }

                    item(key = "payment_breakdown_landscape") {
                        PaymentBreakdownCard(state = state)
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
                item(key = "owner_hero") {
                    OwnerLiveHeroCard(state = state)
                }

                item(key = "stats_row") {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(
                            title = "Active SKUs",
                            value = "${state.stockItems.size}",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Low Stock",
                            value = "${state.lowStockCount}",
                            valueColor = if (state.lowStockCount > 0) Color(0xFFC62828) else LightBluePrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item(key = "executive_actions") {
                    ExecutiveQuickActionsCard(state = state, context = context)
                }

                item(key = "sync_card") {
                    SyncCard(state = state, vm = vm)
                }

                if (state.lowStockCount > 0 || state.expiredCount > 0) {
                    item(key = "alerts_card") {
                        AlertsCard(state = state)
                    }
                }

                item(key = "payment_breakdown") {
                    PaymentBreakdownCard(state = state)
                }
            }
        }
    }
}

@Composable
private fun OwnerLiveHeroCard(state: OwnerDashboardState) {
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Rounded.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Text("Owner Live Business Dashboard", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
            }
            Text(state.totalRevenue.rands(), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                "Today's Net Profit: ${state.netProfit.rands()}  •  ${state.todaySales.size} Transactions",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier, valueColor: Color = LightBluePrimary) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "stat_scale"
    )

    Card(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = LightBlueOnContainer.copy(alpha = 0.7f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = valueColor)
        }
    }
}

@Composable
private fun ExecutiveQuickActionsCard(state: OwnerDashboardState, context: android.content.Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Executive Quick Actions", fontWeight = FontWeight.Bold, color = LightBlueOnContainer, style = MaterialTheme.typography.titleMedium)
            Button(
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
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Generate & Share Audited PDF Statement", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SyncCard(state: OwnerDashboardState, vm: OwnerDashboardViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LightBlueContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.CloudSync, contentDescription = null, tint = LightBluePrimary, modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Cloud Backend Sync", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (state.pendingSyncCount == 0) "All records synced with hbtechinc.co.za" else "${state.pendingSyncCount} records pending sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = vm::syncNow,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sync", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AlertsCard(state: OwnerDashboardState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFDAD6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF410002).copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = Icons.Rounded.Warning, contentDescription = null, tint = Color(0xFF410002), modifier = Modifier.size(20.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Inventory Health Alerts", fontWeight = FontWeight.Bold, color = Color(0xFF410002))
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${state.lowStockCount} items low in stock  •  ${state.expiredCount} items expiring/expired",
                    color = Color(0xFF410002).copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PaymentBreakdownCard(state: OwnerDashboardState) {
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
            if (state.paymentTotals.isEmpty()) {
                Text("No sales recorded today.", color = LightBlueOnContainer.copy(alpha = 0.7f))
            } else {
                state.paymentTotals.forEach { pt ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(pt.method.label, color = LightBlueOnContainer.copy(alpha = 0.8f))
                        Text(pt.total.rands(), fontWeight = FontWeight.Bold, color = LightBluePrimary)
                    }
                }
            }
        }
    }
}