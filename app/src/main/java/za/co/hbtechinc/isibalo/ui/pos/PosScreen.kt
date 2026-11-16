package za.co.hbtechinc.isibalo.ui.pos

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    val total by vm.total.collectAsStateWithLifecycle()

    var selectedItemForScale by remember { mutableStateOf<StockItem?>(null) }
    var selectedLineForNote by remember { mutableStateOf<CartLine?>(null) }

    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.message.collect { snack.showSnackbar(it) } }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.searchQuery.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search products or menu items...") },
                    leadingIcon = { Icon(Icons.Filled.Scale, contentDescription = "Weighing Scale") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Product Catalog & Menu (${items.size})", style = MaterialTheme.typography.titleMedium)
                    Text("⚖️ Veg Scale & Kitchen Ready", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Render product grid in rows of 2 for unified scrolling
            items(items.chunked(2), key = { rowItems -> rowItems.firstOrNull()?.id ?: 0L }) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductTile(item, onClick = { selectedItemForScale = item })
                        }
                    }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                CheckoutPanel(
                    cart = cart, quick = quick, cashTendered = cashTendered, changeDue = changeDue,
                    method = method, orderType = orderType, total = total,
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
        SnackbarHost(snack, Modifier.align(Alignment.TopCenter))
    }

    selectedItemForScale?.let { item ->
        WeighingDialog(
            item = item,
            onDismiss = { selectedItemForScale = null },
            onAddUnit = { vm.add(item); selectedItemForScale = null },
            onAddWeight = { weightKg -> vm.addWeighted(item, weightKg); selectedItemForScale = null }
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
private fun ProductTile(item: StockItem, onClick: () -> Unit) {
    SquareCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text("${item.priceCents.rands()} / unit or kg", color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall)
            Text(
                if (item.quantity <= 0) "Out of stock" else "${item.quantity} in stock",
                color = if (item.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CheckoutPanel(
    cart: List<CartLine>, quick: String, cashTendered: String, changeDue: Long,
    method: PaymentMethod, orderType: String, total: Long,
    onQuick: (String) -> Unit, onCashTendered: (String) -> Unit,
    onMethod: (PaymentMethod) -> Unit, onOrderType: (String) -> Unit,
    onChange: (CartLine, Int) -> Unit, onNoteClick: (CartLine) -> Unit, onCharge: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(color = cs.surfaceVariant, shape = RectangleShape, tonalElevation = 0.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Restaurant Order Type", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("Dine-in", "Takeaway", "Delivery").forEach { type ->
                    SquareChoice(type, type == orderType, { onOrderType(type) }, Modifier.weight(1f))
                }
            }

            Text("Current Cart / Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (cart.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cart.forEach { line ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.weight(1f)) {
                                    Text(line.name, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    Text("${line.qty} x ${line.unitPriceCents.rands()}",
                                        style = MaterialTheme.typography.labelSmall)
                                }
                                SquareIconButton(Icons.Filled.Remove, "Less", { onChange(line, -1) }, Modifier.size(34.dp))
                                Text("${line.qty}", Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.Bold)
                                SquareIconButton(Icons.Filled.Add, "More", { onChange(line, 1) }, Modifier.size(34.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (line.kitchenNote.isNotBlank()) "Note: ${line.kitchenNote}" else "+ Add Kitchen Note / Modifier",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (line.kitchenNote.isNotBlank()) cs.primary else cs.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 2.dp)
                                )
                                SquareButton(
                                    text = if (line.kitchenNote.isNotBlank()) "Edit Note" else "Note",
                                    onClick = { onNoteClick(line) },
                                    modifier = Modifier.height(28.dp),
                                    outlined = true
                                )
                            }
                        }
                    }
                }
            } else {
                Text("Cart is empty. Tap products for scale or quick add.", color = cs.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }

            OutlinedTextField(
                value = quick, onValueChange = onQuick, modifier = Modifier.fillMaxWidth(),
                label = { Text("Quick custom amount (R)") }, singleLine = true, shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Text("Payment Method", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                PaymentMethod.entries.forEach {
                    SquareChoice(it.label, it == method, { onMethod(it) }, Modifier.weight(1f))
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

            SquareButton("CHARGE ${total.rands()}", onCharge, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = total > 0)
        }
    }
}

@Composable
private fun WeighingDialog(
    item: StockItem,
    onDismiss: () -> Unit,
    onAddUnit: () -> Unit,
    onAddWeight: (Double) -> Unit
) {
    var weightStr by remember { mutableStateOf("1.0") }
    val w = weightStr.toDoubleOrNull() ?: 0.0
    val calculatedPrice = (item.priceCents * w).toLong()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.75f).padding(12.dp),
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 6.dp
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⚖️ Weighing Scale / Unit Selector", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Item: ${item.name}", fontWeight = FontWeight.SemiBold)
                Text("Base Rate: ${item.priceCents.rands()} per kg / unit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = weightStr, onValueChange = { weightStr = it },
                    label = { Text("Enter Weight in Kilograms (kg)") },
                    singleLine = true, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Text("Calculated Total: ${calculatedPrice.rands()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Spacer(Modifier.weight(1f))

                SquareButton("ADD AS WEIGHTED (${weightStr} kg - ${calculatedPrice.rands()})", {
                    if (w > 0.0) onAddWeight(w)
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
