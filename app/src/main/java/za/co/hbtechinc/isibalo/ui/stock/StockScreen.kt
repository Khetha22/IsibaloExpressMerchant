package za.co.hbtechinc.isibalo.ui.stock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.ui.components.SquareButton
import za.co.hbtechinc.isibalo.ui.components.SquareCard
import za.co.hbtechinc.isibalo.ui.components.SquareIconButton
import za.co.hbtechinc.isibalo.util.rands
import za.co.hbtechinc.isibalo.util.toCents
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StockScreen(vm: StockViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val items by vm.items.collectAsStateWithLifecycle()
    val low by vm.low.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<StockItem?>(null) }
    var adding by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    val expiringOrExpired = items.filter { it.isExpired || it.isExpiringSoon }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)

    LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { vm.searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search inventory items...") },
                singleLine = true,
                shape = RectangleShape
            )
        }
        item { SquareButton("ADD ITEM", { adding = true }, Modifier.fillMaxWidth()) }

        if (expiringOrExpired.isNotEmpty()) {
            item {
                SquareCard(Modifier.fillMaxWidth(), color = cs.errorContainer) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = cs.onErrorContainer)
                        Column(Modifier.padding(start = 10.dp)) {
                            Text("Perishable Goods Expiry Warning!", fontWeight = FontWeight.Bold, color = cs.onErrorContainer)
                            Text(
                                expiringOrExpired.joinToString { "${it.name} (${if (it.isExpired) "EXPIRED" else "Expiring soon"})" },
                                color = cs.onErrorContainer, style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        if (low.isNotEmpty()) {
            item {
                SquareCard(Modifier.fillMaxWidth(), color = cs.errorContainer) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, null, tint = cs.onErrorContainer)
                        Column(Modifier.padding(start = 10.dp)) {
                            Text("Restock soon", fontWeight = FontWeight.Bold, color = cs.onErrorContainer)
                            Text(low.joinToString { "${it.name} (${it.quantity})" },
                                color = cs.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        items(items, key = { it.id }) { item ->
            SquareCard(Modifier.fillMaxWidth(), onClick = { editing = item }) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        val expiryText = if (item.expiryDate > 0L) "Exp: ${dateFormatter.format(Date(item.expiryDate))}" else "No expiry set"
                        Text("${item.priceCents.rands()}  |  $expiryText",
                            style = MaterialTheme.typography.labelSmall)
                        if (item.isExpired) {
                            Text("EXPIRED", color = cs.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        } else if (item.isExpiringSoon) {
                            Text("EXPIRING SOON", color = cs.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        } else if (item.isLow) {
                            Text("LOW STOCK", color = cs.error, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    SquareIconButton(Icons.Filled.Remove, "Less", { vm.adjust(item, -1) })
                    Text("${item.quantity}", Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (item.isLow || item.isExpired) cs.error else cs.onSurface)
                    SquareIconButton(Icons.Filled.Add, "More", { vm.adjust(item, 1) })
                }
            }
        }
    }

    if (adding || editing != null) {
        ItemDialog(
            initial = editing,
            onDismiss = { adding = false; editing = null },
            onSave = { vm.save(it); adding = false; editing = null },
            onDelete = { editing?.let(vm::delete); adding = false; editing = null }
        )
    }
}

@Composable
private fun ItemDialog(
    initial: StockItem?, onDismiss: () -> Unit,
    onSave: (StockItem) -> Unit, onDelete: () -> Unit
) {
    fun cents(c: Long) = if (c == 0L) "" else String.format(Locale.ROOT, "%.2f", c / 100.0)
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var qty by remember { mutableStateOf(initial?.quantity?.toString() ?: "") }
    var low by remember { mutableStateOf(initial?.lowStockThreshold?.toString() ?: "5") }
    var price by remember { mutableStateOf(cents(initial?.priceCents ?: 0)) }
    var cost by remember { mutableStateOf(cents(initial?.costCents ?: 0)) }
    var daysToExpiry by remember {
        mutableStateOf(
            if (initial != null && initial.expiryDate > 0L)
                ((initial.expiryDate - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).coerceAtLeast(0).toString()
            else ""
        )
    }

    @Composable
    fun field(v: String, set: (String) -> Unit, label: String, num: Boolean = true) =
        OutlinedTextField(
            value = v, onValueChange = set, label = { Text(label) }, singleLine = true,
            shape = RectangleShape, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = if (num) KeyboardType.Decimal else KeyboardType.Text)
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        title = { Text(if (initial == null) "New stock item" else "Edit stock item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                field(name, { name = it }, "Name", num = false)
                field(qty, { qty = it.filter(Char::isDigit) }, "In stock")
                field(low, { low = it.filter(Char::isDigit) }, "Warn when stock reaches")
                field(price, { price = it }, "Selling price (R)")
                field(cost, { cost = it }, "Cost price (R)")
                field(daysToExpiry, { daysToExpiry = it.filter(Char::isDigit) }, "Expiry in Days (0 = None)")
                if (initial != null) SquareButton("DELETE ITEM", onDelete, Modifier.fillMaxWidth(), outlined = true)
            }
        },
        confirmButton = {
            SquareButton("SAVE", {
                val p = price.toCents()
                if (name.isNotBlank() && p != null) {
                    val days = daysToExpiry.toLongOrNull() ?: 0L
                    val expiryTimestamp = if (days > 0) System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000) else 0L
                    onSave(
                        (initial ?: StockItem(name = "", quantity = 0, priceCents = 0)).copy(
                            name = name.trim(),
                            quantity = qty.toIntOrNull() ?: 0,
                            lowStockThreshold = low.toIntOrNull() ?: 5,
                            priceCents = p,
                            costCents = cost.toCents() ?: 0L,
                            expiryDate = expiryTimestamp
                        )
                    )
                }
            })
        },
        dismissButton = { SquareButton("CANCEL", onDismiss, outlined = true) }
    )
}
