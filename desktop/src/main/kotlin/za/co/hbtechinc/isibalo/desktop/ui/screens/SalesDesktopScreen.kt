package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.data.PaymentMethod
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesDesktopScreen(repository: DesktopRepository) {
    val salesList by repository.sales.collectAsState()
    val fmt = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    var shareMessageDialog by remember { mutableStateOf<String?>(null) }

    val totalRevenue = salesList.sumOf { it.sale.totalCents }
    val avgBasket = if (salesList.isNotEmpty()) totalRevenue / salesList.size else 0L

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Top Revenue Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = LightBluePrimary)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Today's Total Store Revenue", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                Text(totalRevenue.rands(), color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text("${salesList.size} Transactions Recorded • Average Basket: ${avgBasket.rands()}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            }
        }

        // Breakdown by Payment Method
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PaymentMethod.entries.forEach { method ->
                val methodTotal = salesList.filter { it.sale.method == method }.sumOf { it.sale.totalCents }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, LightBlueContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(method.label, style = MaterialTheme.typography.labelMedium, color = LightBlueSecondary)
                        Text(methodTotal.rands(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                    }
                }
            }
        }

        Text("Transactions Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

        // Transactions List
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(salesList, key = { it.sale.id }) { saleWithItems ->
                val sale = saleWithItems.sale
                val items = saleWithItems.items
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
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Sale #${sale.id}", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                Surface(shape = RoundedCornerShape(8.dp), color = LightBlueContainer) {
                                    Text(sale.method.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = LightBluePrimary, fontWeight = FontWeight.Bold)
                                }
                                Text(fmt.format(Date(sale.createdAt)), style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                            }

                            Text(
                                items.joinToString { "${it.name} x${it.qty}" }.ifBlank { "Direct Payment" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightBlueSecondary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(sale.totalCents.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                            Button(
                                onClick = {
                                    val summary = "Isibalo Express Receipt #${sale.id}: ${sale.totalCents.rands()} paid via ${sale.method.label}."
                                    repository.logWhatsAppShare("Sale #${sale.id} Receipt", summary)
                                    shareMessageDialog = summary
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBluePrimary)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = "Share Receipt")
                                Spacer(Modifier.width(6.dp))
                                Text("WhatsApp")
                            }
                        }
                    }
                }
            }
        }
    }

    shareMessageDialog?.let { summary ->
        AlertDialog(
            onDismissRequest = { shareMessageDialog = null },
            confirmButton = { TextButton(onClick = { shareMessageDialog = null }) { Text("Close") } },
            title = { Text("Receipt Share Logged") },
            text = { Text("WhatsApp digital receipt logged to audit database:\n\n$summary") }
        )
    }
}
