package za.co.hbtechinc.isibalo.desktop.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import za.co.hbtechinc.isibalo.desktop.data.*
import za.co.hbtechinc.isibalo.desktop.ui.*
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.toCents

@Composable
fun PosDesktopScreen(repository: DesktopRepository) {
    val stockList by repository.stock.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Retailer") }
    var orderType by remember { mutableStateOf("Takeaway") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var quickAmountText by remember { mutableStateOf("") }
    var cashTenderedText by remember { mutableStateOf("") }

    val cart = remember { mutableStateListOf<CartLine>() }
    var scaleItem by remember { mutableStateOf<StockItem?>(null) }
    var scaleWeightKgText by remember { mutableStateOf("1.0") }
    var checkoutSuccessMessage by remember { mutableStateOf<String?>(null) }

    val filteredItems = remember(stockList, searchQuery) {
        if (searchQuery.isBlank()) stockList
        else stockList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val totalCents = remember(cart.toList()) {
        cart.sumOf { it.unitPriceCents * it.qty }
    }

    val cashTenderedCents = cashTenderedText.toCents() ?: 0L
    val changeDueCents = (cashTenderedCents - totalCents).coerceAtLeast(0L)

    Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left Column: Catalog & Search
        Column(modifier = Modifier.weight(1.4f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Search & Mode Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Catalog & Point of Sale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Retailer", "Restaurant").forEach { type ->
                                FilterChip(
                                    selected = businessType == type,
                                    onClick = { businessType = type },
                                    label = { Text(type) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(if (businessType == "Restaurant") "Search menu items..." else "Search items or scale goods...") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Products Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                val existingIndex = cart.indexOfFirst { it.stockItemId == item.id }
                                if (existingIndex >= 0) {
                                    val current = cart[existingIndex]
                                    if (current.qty < item.quantity) {
                                        cart[existingIndex] = current.copy(qty = current.qty + 1)
                                    }
                                } else if (item.quantity > 0) {
                                    cart.add(CartLine(stockItemId = item.id, name = item.name, unitPriceCents = item.priceCents, qty = 1, maxQty = item.quantity))
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, LightBlueContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LightBlueOnContainer
                                )
                                if (item.isLow) {
                                    Surface(shape = RoundedCornerShape(8.dp), color = ErrorContainerLight) {
                                        Text("Low", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = OnErrorContainerLight)
                                    }
                                }
                            }
                            Text(text = item.priceCents.rands(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("In stock: ${item.quantity}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                IconButton(onClick = { scaleItem = item }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Rounded.Scale, contentDescription = "Weigh Item", tint = LightBluePrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Custom Amount Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquareField(
                        value = quickAmountText,
                        onValueChange = { quickAmountText = it },
                        label = "Quick Custom Amount (Rands)",
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = {
                            val cents = quickAmountText.toCents()
                            if (cents != null && cents > 0) {
                                cart.add(CartLine(stockItemId = null, name = "Custom Item", unitPriceCents = cents, qty = 1))
                                quickAmountText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Text("Add Custom")
                    }
                }
            }
        }

        // Right Column: Checkout Cart Panel
        Card(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, LightBlueContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Current Order Cart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    TextButton(onClick = { cart.clear() }) { Text("Clear All", color = MaterialTheme.colorScheme.error) }
                }

                // Order Type Chips
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Dine-in", "Takeaway", "Delivery").forEach { type ->
                        FilterChip(
                            selected = orderType == type,
                            onClick = { orderType = type },
                            label = { Text(type) }
                        )
                    }
                }

                // Cart Line Items List
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (cart.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("Cart is empty. Click items to add.", style = MaterialTheme.typography.bodyMedium, color = LightBlueSecondary)
                            }
                        }
                    } else {
                        items(cart.toList()) { line ->
                            val index = cart.indexOf(line)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = LightBlueSurface
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(line.name, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                                        Text("${line.unitPriceCents.rands()} x ${line.qty} = ${(line.unitPriceCents * line.qty).rands()}", style = MaterialTheme.typography.bodySmall, color = LightBlueSecondary)
                                        if (line.weightKg != null) {
                                            Text("Scale Weight: ${line.weightKg} kg", style = MaterialTheme.typography.labelSmall, color = LightBluePrimary)
                                        }
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            if (index >= 0) {
                                                if (line.qty > 1) cart[index] = line.copy(qty = line.qty - 1)
                                                else cart.removeAt(index)
                                            }
                                        }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Rounded.Remove, contentDescription = "Decrease")
                                        }
                                        Text("${line.qty}", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                        IconButton(onClick = {
                                            if (index >= 0 && line.qty < line.maxQty) {
                                                cart[index] = line.copy(qty = line.qty + 1)
                                            }
                                        }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Rounded.Add, contentDescription = "Increase")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()

                // Total & Tendered
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cart Total Amount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(totalCents.rands(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = LightBluePrimary)
                    }

                    // Payment Method Options
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = LightBlueSecondary)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (paymentMethod == PaymentMethod.CASH) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            SquareField(
                                value = cashTenderedText,
                                onValueChange = { cashTenderedText = it },
                                label = "Cash Tendered",
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Column {
                                Text("Change Due", style = MaterialTheme.typography.labelSmall, color = LightBlueSecondary)
                                Text(changeDueCents.rands(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBluePrimary)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (cart.isNotEmpty()) {
                                repository.checkout(cart.toList(), paymentMethod)
                                cart.clear()
                                cashTenderedText = ""
                                checkoutSuccessMessage = "Sale recorded successfully in SQLite!"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = cart.isNotEmpty(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                    ) {
                        Text("Complete Sale (${totalCents.rands()})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // Scale Dialog for Weighted Products
    scaleItem?.let { item ->
        Dialog(onDismissRequest = { scaleItem = null }) {
            Card(modifier = Modifier.width(360.dp), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Scale Weight Calculator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                    Text("Product: ${item.name} (${item.priceCents.rands()}/kg)", style = MaterialTheme.typography.bodyMedium)

                    SquareField(
                        value = scaleWeightKgText,
                        onValueChange = { scaleWeightKgText = it },
                        label = "Weight (Kilograms)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    val weightKg = scaleWeightKgText.toDoubleOrNull() ?: 1.0
                    val calcCents = (item.priceCents * weightKg).toLong()

                    Text("Calculated Total: ${calcCents.rands()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = LightBluePrimary)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { scaleItem = null }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                cart.add(CartLine(stockItemId = item.id, name = "${item.name} (${weightKg}kg)", unitPriceCents = calcCents, qty = 1, weightKg = weightKg))
                                scaleItem = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary)
                        ) {
                            Text("Add to Cart")
                        }
                    }
                }
            }
        }
    }

    // Checkout Confirmation Dialog
    checkoutSuccessMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { checkoutSuccessMessage = null },
            confirmButton = { TextButton(onClick = { checkoutSuccessMessage = null }) { Text("OK") } },
            title = { Text("Checkout Complete") },
            text = { Text(msg) }
        )
    }
}
