package za.co.hbtechinc.isibalo.ui.pos

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.CartLine
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.ui.components.SquareChoice
import za.co.hbtechinc.isibalo.ui.components.SquareIconButton
import za.co.hbtechinc.isibalo.util.rands

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(vm: PosViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val items by vm.items.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val cart by vm.cart.collectAsStateWithLifecycle()
    val quick by vm.quickAmount.collectAsStateWithLifecycle()
    val cashTendered by vm.cashTendered.collectAsStateWithLifecycle()
    val changeDue by vm.changeDue.collectAsStateWithLifecycle()
    val method by vm.method.collectAsStateWithLifecycle()
    val orderType by vm.orderType.collectAsStateWithLifecycle()
    val businessType by vm.businessType.collectAsStateWithLifecycle()
    val isSheetOpen by vm.isCheckoutSheetOpen.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()

    var selectedItemForScale by remember { mutableStateOf<StockItem?>(null) }
    var selectedLineForNote by remember { mutableStateOf<CartLine?>(null) }

    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.message.collect { snack.showSnackbar(it) } }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Top Bar: Business Type Toggle & Search
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Retailer", "Restaurant").forEach { type ->
                    SquareChoice(type, type == businessType, { vm.setBusinessType(type) }, Modifier.weight(1f))
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { vm.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                placeholder = { Text(if (businessType == "Restaurant") "Search menu items..." else "Search products or weighted items...") },
                leadingIcon = { Icon(Icons.Filled.Scale, contentDescription = "Scale") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Catalog Grid
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (businessType == "Restaurant") "Menu Catalog (${items.size})" else "Catalog & Sensor Scale (${items.size})", style = MaterialTheme.typography.titleMedium)
                        Text("⚖️ Grams, kg, ml, Liters", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }

                items(items.chunked(2), key = { rowItems -> rowItems.firstOrNull()?.id ?: 0L }) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductCard(
                                    item = item,
                                    onClick = {
                                        if (item.quantity > 0) {
                                            selectedItemForScale = item
                                        }
                                    }
                                )
                            }
                        }
                        if (rowItems.size == 1) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Persistent Sticky Bottom Bar Summary
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Cart: ${cart.sumOf { it.qty }} items", style = MaterialTheme.typography.labelMedium)
                        Text("Total: ${total.rands()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    SquareButton(
                        text = "PROCEED TO CHARGE ➔",
                        onClick = { vm.setCheckoutSheetOpen(true) },
                        enabled = total > 0
                    )
                }
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.TopCenter))
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { vm.setCheckoutSheetOpen(false) }
        ) {
            CheckoutBottomSheet(
                cart = cart, quick = quick, cashTendered = cashTendered, changeDue = changeDue,
                method = method, orderType = orderType, businessType = businessType, total = total,
                onQuick = vm::setQuick,
                onCashTendered = vm::setCashTendered,
                onMethod = { vm.method.value = it },
                onOrderType = vm::setOrderType,
                onChange = vm::change,
                onNoteClick = { selectedLineForNote = it },
                onCharge = vm::charge
            )
        }
    }

    selectedItemForScale?.let { item ->
        WeighingDialog(
            item = item,
            onDismiss = { selectedItemForScale = null },
            onAddUnit = { vm.add(item); selectedItemForScale = null },
            onAddWeight = { weightKgOrL -> vm.addWeighted(item, weightKgOrL); selectedItemForScale = null }
        )
    }

    selectedLineForNote?.let { line ->
        KitchenNoteDialog(
            line = line,
            onDismiss = { selectedLineForNote = null },
            onSave = { note -> vm.updateKitchenNote(line, note); selectedLineForNote = null }
        )
    }
}

@Composable
private fun ProductCard(item: StockItem, onClick: () -> Unit) {
    val isAvailable = item.quantity > 0
    SquareCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isAvailable) 1.0f else 0.5f),
        onClick = { if (isAvailable) onClick() }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text("${item.priceCents.rands()} / kg, L, unit", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall)
            if (!isAvailable) {
                Text("Out of stock", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            } else {
                Text("${item.quantity} in stock", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CheckoutBottomSheet(
    cart: List<CartLine>, quick: String, cashTendered: String, changeDue: Long,
    method: PaymentMethod, orderType: String, businessType: String, total: Long,
    onQuick: (String) -> Unit, onCashTendered: (String) -> Unit,
    onMethod: (PaymentMethod) -> Unit, onOrderType: (String) -> Unit,
    onChange: (CartLine, Int) -> Unit, onNoteClick: (CartLine) -> Unit, onCharge: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 650.dp)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Secure Checkout & Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (businessType == "Restaurant") {
            Text("Order Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Dine-in", "Takeaway", "Delivery").forEach { type ->
                    SquareChoice(type, type == orderType, { onOrderType(type) }, Modifier.weight(1f))
                }
            }
        }

        Text("Cart Summary (${cart.sumOf { it.qty }} items)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (cart.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cart.forEach { line ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                Text(line.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                Text("${line.qty} x ${line.unitPriceCents.rands()}", style = MaterialTheme.typography.labelSmall)
                            }
                            SquareIconButton(Icons.Filled.Remove, "Less", { onChange(line, -1) }, Modifier.size(32.dp))
                            Text("${line.qty}", Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                            SquareIconButton(Icons.Filled.Add, "More", { onChange(line, 1) }, Modifier.size(32.dp))
                        }
                        if (businessType == "Restaurant") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (line.kitchenNote.isNotBlank()) "Note: ${line.kitchenNote}" else "+ Add Kitchen Note",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (line.kitchenNote.isNotBlank()) cs.primary else cs.onSurfaceVariant
                                )
                                SquareButton("Note", { onNoteClick(line) }, Modifier.height(26.dp), outlined = true)
                            }
                        }
                    }
                }
            }
        }

        OutlinedTextField(
            value = quick, onValueChange = onQuick, modifier = Modifier.fillMaxWidth(),
            label = { Text("Quick custom amount (R)") }, singleLine = true, shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Text("Payment Method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMethod.entries.forEach { pm ->
                FilterChip(
                    selected = pm == method,
                    onClick = { onMethod(pm) },
                    label = { Text(text = pm.label) }
                )
            }
        }

        if (method == PaymentMethod.CASH) {
            SquareCard(Modifier.fillMaxWidth(), color = cs.surface) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Cash Tendered & Change Calculator", fontWeight = FontWeight.Bold, color = cs.primary)
                    OutlinedTextField(
                        value = cashTendered, onValueChange = onCashTendered, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Amount Tendered by Customer (R)") }, singleLine = true, shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Total Due: ${total.rands()}", fontWeight = FontWeight.SemiBold)
                        Text("Change Due: ${changeDue.rands()}", fontWeight = FontWeight.Bold, color = cs.primary, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        SquareButton("CHARGE ${total.rands()}", onCharge, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = total > 0)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun WeighingDialog(
    item: StockItem,
    onDismiss: () -> Unit,
    onAddUnit: () -> Unit,
    onAddWeight: (Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("1.0") }
    var selectedUnit by remember { mutableStateOf("kg") } // kg, g, L, ml
    
    val rawVal = amountStr.toDoubleOrNull() ?: 0.0
    val weightOrVolumeKgOrL = when (selectedUnit) {
        "g" -> rawVal / 1000.0
        "ml" -> rawVal / 1000.0
        else -> rawVal
    }
    val calculatedPrice = (item.priceCents * weightOrVolumeKgOrL).toLong()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("⚖️ Sensor Scale (Weight & Volume)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Item: ${item.name}", fontWeight = FontWeight.SemiBold)
                Text("Base Rate: ${item.priceCents.rands()} per kg / liter", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Text("Select Measurement Unit", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("kg", "g", "L", "ml").forEach { unit ->
                        SquareChoice(unit, unit == selectedUnit, { selectedUnit = unit }, Modifier.weight(1f))
                    }
                }

                OutlinedTextField(
                    value = amountStr, onValueChange = { amountStr = it },
                    label = { Text("Enter Quantity ($selectedUnit)") },
                    singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Text("Calculated Total: ${calculatedPrice.rands()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.weight(1f))

                SquareButton("ADD AS SENSOR MEASURED (${amountStr} $selectedUnit - ${calculatedPrice.rands()})", {
                    if (weightOrVolumeKgOrL > 0.0) onAddWeight(weightOrVolumeKgOrL)
                }, Modifier.fillMaxWidth())

                SquareButton("ADD AS 1 STANDARD UNIT", onAddUnit, Modifier.fillMaxWidth(), outlined = true)

                SquareButton("CANCEL", onDismiss, Modifier.fillMaxWidth(), outlined = true)
            }
        }
    }
}

@Composable
private fun KitchenNoteDialog(
    line: CartLine,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by remember { mutableStateOf(line.kitchenNote) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.7f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🍳 Kitchen Order Modifier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Item: ${line.name}", fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("Kitchen Note (e.g. No onions, Extra cheese)") },
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))

                SquareButton("SAVE MODIFIER", { onSave(note) }, Modifier.fillMaxWidth())
                SquareButton("CANCEL", onDismiss, Modifier.fillMaxWidth(), outlined = true)
            }
        }
    }
}
