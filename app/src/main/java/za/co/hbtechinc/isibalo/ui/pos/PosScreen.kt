package za.co.hbtechinc.isibalo.ui.pos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    val method by vm.method.collectAsStateWithLifecycle()
    val total by vm.total.collectAsStateWithLifecycle()
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
                    placeholder = { Text("Search products to sell...") },
                    singleLine = true,
                    shape = RectangleShape
                )
            }

            item {
                Text("Product Catalog (${items.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            }

            // Render product grid in rows of 2 for unified scrolling
            items(items.chunked(2), key = { rowItems -> rowItems.firstOrNull()?.id ?: 0L }) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductTile(item) { vm.add(item) }
                        }
                    }
                    if (rowItems.size == 1) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                CheckoutPanel(
                    cart = cart, quick = quick, method = method, total = total,
                    onQuick = vm::setQuick,
                    onMethod = { vm.method.value = it },
                    onChange = vm::change,
                    onCharge = vm::charge
                )
            }
        }
        SnackbarHost(snack, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun ProductTile(item: StockItem, onClick: () -> Unit) {
    SquareCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(12.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
            Text(item.priceCents.rands(), color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge)
            Text(
                if (item.quantity <= 0) "Out of stock" else "${item.quantity} left",
                color = if (item.isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun CheckoutPanel(
    cart: List<CartLine>, quick: String, method: PaymentMethod, total: Long,
    onQuick: (String) -> Unit, onMethod: (PaymentMethod) -> Unit,
    onChange: (CartLine, Int) -> Unit, onCharge: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RectangleShape, tonalElevation = 0.dp) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Current Cart / Checkout", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (cart.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    cart.forEach { line ->
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
                    }
                }
            } else {
                Text("Cart is empty. Tap products above to add.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            OutlinedTextField(
                value = quick, onValueChange = onQuick, modifier = Modifier.fillMaxWidth(),
                label = { Text("Quick amount (R)") }, singleLine = true, shape = RectangleShape,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach {
                    SquareChoice(it.label, it == method, { onMethod(it) }, Modifier.weight(1f))
                }
            }
            SquareButton("CHARGE ${total.rands()}", onCharge, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = total > 0)
        }
    }
}
