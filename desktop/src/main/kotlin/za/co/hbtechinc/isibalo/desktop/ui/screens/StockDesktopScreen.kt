package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.data.StockItem
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.toCents

@Composable
fun StockDesktopScreen(repository: DesktopRepository) {
    val stockItems by repository.stock.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<StockItem?>(null) }
    var isAdding by remember { mutableStateOf(false) }

    val filtered = remember(stockItems, searchQuery) {
        if (searchQuery.isBlank()) stockItems
        else stockItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val lowStockCount = stockItems.count { it.isLow }
    val totalAssetValue = stockItems.sumOf { it.priceCents * it.quantity }
    val expiredOrExpiring = stockItems.filter { it.isExpired || it.isExpiringSoon }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hero Inventory Summary
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
                    Text("Inventory & Stock Management", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    Text("Total Inventory Asset Value: ${totalAssetValue.rands()}", style = MaterialTheme.typography.bodyMedium, color = LightBlueSecondary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(shape = RoundedCornerShape(12.dp), color = LightBlueContainer) {
                        Text("${stockItems.size} SKUs", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = LightBluePrimary)
                    }
                    if (lowStockCount > 0) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ErrorContainerLight) {
                            Text("$lowStockCount Low Stock Alerts", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontWeight = FontWeight.Bold, color = OnErrorContainerLight)
                        }
                    }
                    Button(
                        onClick = { isAdding = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Product")
                        Spacer(Modifier.width(6.dp))
                        Text("Add New Product")
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search stock by SKU product name...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (expiredOrExpiring.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorContainerLight)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Warning, contentDescription = "Alert", tint = OnErrorContainerLight)
                    Text(
                        "Product Expiry Alert: ${expiredOrExpiring.size} items are expired or expiring within 7 days!",
                        fontWeight = FontWeight.Bold,
                        color = OnErrorContainerLight
                    )
                }
            }
        }

        // Stock Items List
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, if (item.isLow) ErrorContainerLight else LightBlueContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Text("Cost: ${item.costCents.rands()}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                Text("Selling: ${item.priceCents.rands()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                                Text("Min threshold: ${item.lowStockThreshold}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Quantity Controls
                            Surface(shape = RoundedCornerShape(12.dp), color = LightBlueSurface) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                                    IconButton(onClick = {
                                        if (item.quantity > 0) {
                                            repository.saveStock(item.copy(quantity = item.quantity - 1))
                                        }
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                                    }
                                    Text("${item.quantity}", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                                    IconButton(onClick = {
                                        repository.saveStock(item.copy(quantity = item.quantity + 1))
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Rounded.Add, contentDescription = "Increase")
                                    }
                                }
                            }

                            IconButton(onClick = { editingItem = item }) {
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit SKU", tint = LightBluePrimary)
                            }

                            IconButton(onClick = { repository.deleteStock(item) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete SKU", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal for Add / Edit Stock Item
    if (isAdding || editingItem != null) {
        val target = editingItem
        var nameText by remember { mutableStateOf(target?.name ?: "") }
        var qtyText by remember { mutableStateOf(target?.quantity?.toString() ?: "10") }
        var thresholdText by remember { mutableStateOf(target?.lowStockThreshold?.toString() ?: "5") }
        var costText by remember { mutableStateOf(if (target != null) String.format("%.2f", target.costCents / 100.0) else "") }
        var priceText by remember { mutableStateOf(if (target != null) String.format("%.2f", target.priceCents / 100.0) else "") }

        Dialog(onDismissRequest = { isAdding = false; editingItem = null }) {
            Card(modifier = Modifier.width(420.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (target == null) "Add New Product SKU" else "Edit Product Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)

                    SquareField(value = nameText, onValueChange = { nameText = it }, label = "Product Name")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SquareField(value = qtyText, onValueChange = { qtyText = it }, label = "Initial Qty", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        SquareField(value = thresholdText, onValueChange = { thresholdText = it }, label = "Low Alert Min Qty", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SquareField(value = costText, onValueChange = { costText = it }, label = "Cost Price (R)", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        SquareField(value = priceText, onValueChange = { priceText = it }, label = "Selling Price (R)", modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { isAdding = false; editingItem = null }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val priceCents = priceText.toCents() ?: 0L
                                val costCents = costText.toCents() ?: 0L
                                val qty = qtyText.toIntOrNull() ?: 0
                                val thresh = thresholdText.toIntOrNull() ?: 5
                                if (nameText.isNotBlank() && priceCents > 0) {
                                    val item = target?.copy(
                                        name = nameText,
                                        quantity = qty,
                                        lowStockThreshold = thresh,
                                        costCents = costCents,
                                        priceCents = priceCents
                                    ) ?: StockItem(
                                        name = nameText,
                                        quantity = qty,
                                        lowStockThreshold = thresh,
                                        costCents = costCents,
                                        priceCents = priceCents
                                    )
                                    repository.saveStock(item)
                                    isAdding = false
                                    editingItem = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                        ) {
                            Text("Save Product")
                        }
                    }
                }
            }
        }
    }
}
