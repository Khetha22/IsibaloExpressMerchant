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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory
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
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.util.rands

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@Composable
fun StockTakeScreen(vm: StockTakeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
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
                // Left Column: Valuation Hero, Health Audit, & WhatsApp Share
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StockTakeHeroCard(report = report)
                    InventoryHealthCard(report = report)
                    ShareStockButton(report = report, context = context)
                }

                // Right Column: Complete Inventory Valuation Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "inventory_list_header_landscape") {
                        Text(
                            text = "Complete Inventory Valuation List (${report.items.size} SKUs)",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(report.items, key = { it.id }) { item ->
                        StockItemCard(item = item)
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
                item(key = "stock_hero") {
                    StockTakeHeroCard(report = report)
                }

                item(key = "health_audit") {
                    InventoryHealthCard(report = report)
                }

                item(key = "share_stock_btn") {
                    ShareStockButton(report = report, context = context)
                }

                item(key = "inventory_list_header") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Complete Inventory Valuation List (${report.items.size} SKUs)",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(report.items, key = { it.id }) { item ->
                    StockItemCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun StockTakeHeroCard(report: StockTakeState) {
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
                        Icon(imageVector = Icons.Rounded.Inventory, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
                Text("Stock Take & Inventory Audit", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.labelLarge)
            }
            Text("Retail Valuation: ${report.totalRetailValueCents.rands()}", color = Color.White, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
            Text(
                "Total Units: ${report.totalUnits} (${report.items.size} SKUs)  •  Cost: ${report.totalCostValueCents.rands()}",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InventoryHealthCard(report: StockTakeState) {
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
            Text("Inventory Health Audit", fontWeight = FontWeight.Bold, color = LightBlueOnContainer, style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Low Stock Items:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(
                    "${report.lowStockCount} SKUs",
                    fontWeight = FontWeight.Bold,
                    color = if (report.lowStockCount > 0) Color(0xFFC62828) else LightBluePrimary
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Expired / Expiring Soon:", color = LightBlueOnContainer.copy(alpha = 0.8f))
                Text(
                    "${report.expiredCount} SKUs",
                    fontWeight = FontWeight.Bold,
                    color = if (report.expiredCount > 0) Color(0xFFC62828) else LightBluePrimary
                )
            }
        }
    }
}

@Composable
private fun StockItemCard(item: StockItem) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "stock_item_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightBlueOnContainer
                )
                Text(
                    text = "Qty: ${item.quantity} units  •  Cost: ${item.costCents.rands()}  •  Price: ${item.priceCents.rands()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Total Value: ${(item.priceCents * item.quantity).rands()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = LightBluePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ShareStockButton(report: StockTakeState, context: android.content.Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "share_stock_scale"
    )

    Button(
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
        Text("Share Stock Take Audit via WhatsApp", fontWeight = FontWeight.Bold)
    }
}
