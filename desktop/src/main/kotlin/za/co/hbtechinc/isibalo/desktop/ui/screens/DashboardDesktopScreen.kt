package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.CloudSync
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
fun DashboardDesktopScreen(repository: DesktopRepository, onNavigate: (DesktopNav) -> Unit) {
    val stockItems by repository.stock.collectAsState()
    val sales by repository.sales.collectAsState()
    val debts by repository.customerDebts.collectAsState()

    var actionMessage by remember { mutableStateOf<String?>(null) }

    val todayRevenue = sales.sumOf { it.sale.totalCents }
    val lowStockCount = stockItems.count { it.isLow }
    val openDebtsTotal = debts.filter { !it.settled }.sumOf { it.amountCents }
    val avgBasket = if (sales.isNotEmpty()) todayRevenue / sales.size else 0L

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Revenue Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = LightBluePrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Today's Store Revenue", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                Text(todayRevenue.rands(), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text("${sales.size} Transactions Recorded • Average Basket ${avgBasket.rands()}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            DesktopStatCard("Active Inventory SKUs", "${stockItems.size} SKUs", Modifier.weight(1f))
            DesktopStatCard("Low Stock Warnings", "$lowStockCount SKUs", Modifier.weight(1f), isAlert = lowStockCount > 0)
            DesktopStatCard("Outstanding Credit Tabs", openDebtsTotal.rands(), Modifier.weight(1f))
        }

        Text("Quick Operations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { onNavigate(DesktopNav.POS) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Text("Launch Point of Sale (POS)", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { onNavigate(DesktopNav.Reports) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Rounded.Assessment, contentDescription = "Reports")
                Spacer(Modifier.width(6.dp))
                Text("Financial P&L Reports", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    repository.refreshAll()
                    actionMessage = "Offline transactions synced with SQLite local database!"
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                modifier = Modifier.weight(1f).height(50.dp)
            ) {
                Icon(Icons.Rounded.CloudSync, contentDescription = "Sync")
                Spacer(Modifier.width(6.dp))
                Text("Sync Offline DB", fontWeight = FontWeight.Bold)
            }
        }

        Text("Recent Transaction Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
        sales.take(5).forEach { saleWithItems ->
            val sale = saleWithItems.sale
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
                        Text("Sale #${sale.id} • ${sale.method.label}", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                        Text("${saleWithItems.items.size} line items", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                    }
                    Text(sale.totalCents.rands(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                }
            }
        }
    }

    actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            confirmButton = { TextButton(onClick = { actionMessage = null }) { Text("OK") } },
            title = { Text("Database Sync") },
            text = { Text(msg) }
        )
    }
}

@Composable
private fun DesktopStatCard(title: String, value: String, modifier: Modifier = Modifier, isAlert: Boolean = false) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isAlert) ErrorContainerLight else LightBlueSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = if (isAlert) OnErrorContainerLight else LightBlueOnContainer.copy(alpha = 0.7f))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (isAlert) OnErrorContainerLight else LightBluePrimary)
        }
    }
}
