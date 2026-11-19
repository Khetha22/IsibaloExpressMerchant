package za.co.hbtechinc.isibalo.ui.stock

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.ui.components.SquareField
import za.co.hbtechinc.isibalo.util.rands
import za.co.hbtechinc.isibalo.util.toCents
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Consistent Light Blue palette matching Isibalo design system
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)

@Composable
fun StockScreen(vm: StockViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val items by vm.items.collectAsStateWithLifecycle()
    val low by vm.low.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<StockItem?>(null) }
    var adding by remember { mutableStateOf(false) }

    val expiringOrExpired = items.filter { it.isExpired || it.isExpiringSoon }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)

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
                // Left Column: Hero Summary, Search, Add Action, and Health Alerts
                Column(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    StockHeroCard(items = items, lowCount = low.size)
                    StockSearchBar(query = query, onQueryChanged = { vm.searchQuery.value = it })
                    AddItemButton(onClick = { adding = true })

                    if (expiringOrExpired.isNotEmpty()) {
                        ExpiryAlertCard(expiringOrExpired = expiringOrExpired)
                    }
                    if (low.isNotEmpty()) {
                        LowStockAlertCard(low = low)
                    }
                }

                // Right Column: Inventory Catalog Feed
                LazyColumn(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight(),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "stock_header_landscape") {
                        Text(
                            text = "Inventory Catalog (${items.size} SKUs)",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (items.isEmpty()) {
                        item(key = "empty_stock_landscape") {
                            Text(
                                text = "No stock items matching your search.",
                                color = LightBlueOnContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(items, key = { it.id }) { item ->
                            StockCardItem(
                                item = item,
                                dateFormatter = dateFormatter,
                                onEdit = { editing = item },
                                onAdjust = { delta -> vm.adjust(item, delta) }
                            )
                        }
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
                    StockHeroCard(items = items, lowCount = low.size)
                }

                item(key = "stock_search") {
                    StockSearchBar(query = query, onQueryChanged = { vm.searchQuery.value = it })
                }

                item(key = "add_item_btn") {
                    AddItemButton(onClick = { adding = true })
                }

                if (expiringOrExpired.isNotEmpty()) {
                    item(key = "expiry_alert") {
                        ExpiryAlertCard(expiringOrExpired = expiringOrExpired)
                    }
                }

                if (low.isNotEmpty()) {
                    item(key = "low_stock_alert") {
                        LowStockAlertCard(low = low)
                    }
                }

                item(key = "stock_list_header") {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Inventory Catalog (${items.size} SKUs)",
                        style = MaterialTheme.typography.titleSmall,
                        color = LightBluePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (items.isEmpty()) {
                    item(key = "empty_stock") {
                        Text(
                            text = "No stock items matching your search.",
                            color = LightBlueOnContainer.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(items, key = { it.id }) { item ->
                        StockCardItem(
                            item = item,
                            dateFormatter = dateFormatter,
                            onEdit = { editing = item },
                            onAdjust = { delta -> vm.adjust(item, delta) }
                        )
                    }
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
private fun StockHeroCard(items: List<StockItem>, lowCount: Int) {
    val totalUnits = items.sumOf { it.quantity }
    val retailValuation = items.sumOf { it.priceCents * it.quantity }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Inventory2,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Stock & Inventory Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = retailValuation.rands(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "$totalUnits units across ${items.size} SKUs  •  $lowCount low stock",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun StockSearchBar(query: String, onQueryChanged: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 1.dp
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp)),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search Icon",
                    tint = LightBluePrimary
                )
            },
            placeholder = {
                Text(
                    text = "Search inventory items...",
                    color = LightBlueOnContainer.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = LightBlueOnContainer,
                unfocusedTextColor = LightBlueOnContainer
            )
        )
    }
}

@Composable
private fun AddItemButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "add_stock_btn_scale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("ADD STOCK ITEM", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ExpiryAlertCard(expiringOrExpired: List<StockItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorContainerLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnErrorContainerLight.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = OnErrorContainerLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Perishable Goods Expiry Warning!",
                    fontWeight = FontWeight.Bold,
                    color = OnErrorContainerLight,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = expiringOrExpired.joinToString { "${it.name} (${if (it.isExpired) "EXPIRED" else "Expiring soon"})" },
                    color = OnErrorContainerLight.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LowStockAlertCard(low: List<StockItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ErrorContainerLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnErrorContainerLight.copy(alpha = 0.12f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = OnErrorContainerLight,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Restock Soon",
                    fontWeight = FontWeight.Bold,
                    color = OnErrorContainerLight,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = low.joinToString { "${it.name} (${it.quantity} left)" },
                    color = OnErrorContainerLight.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StockCardItem(
    item: StockItem,
    dateFormatter: SimpleDateFormat,
    onEdit: () -> Unit,
    onAdjust: (Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "stock_card_scale"
    )

    val expiryText = if (item.expiryDate > 0L) "Exp: ${dateFormatter.format(Date(item.expiryDate))}" else "No expiry set"
    val profitC = item.priceCents - item.costCents
    val margin = if (item.priceCents > 0) (profitC.toDouble() / item.priceCents * 100).toInt() else 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )
                Text(
                    text = "${item.priceCents.rands()}  •  Profit: ${profitC.rands()} ($margin%)",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBluePrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = expiryText,
                    style = MaterialTheme.typography.labelSmall,
                    color = LightBlueOnContainer.copy(alpha = 0.65f)
                )

                val badgeText = when {
                    item.isExpired -> "EXPIRED"
                    item.isExpiringSoon -> "EXPIRING SOON"
                    item.isLow -> "LOW STOCK"
                    else -> null
                }

                if (badgeText != null) {
                    Spacer(Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ErrorContainerLight
                    ) {
                        Text(
                            text = badgeText,
                            color = OnErrorContainerLight,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onAdjust(-1) },
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlueContainer,
                        contentColor = LightBlueOnContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Less", modifier = Modifier.size(18.dp))
                }

                Text(
                    text = "${item.quantity}",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isLow || item.isExpired) Color(0xFFC62828) else LightBlueOnContainer
                )

                Button(
                    onClick = { onAdjust(1) },
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBluePrimary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "More", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun ItemDialog(
    initial: StockItem?,
    onDismiss: () -> Unit,
    onSave: (StockItem) -> Unit,
    onDelete: () -> Unit
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

    val pCents = price.toCents() ?: 0L
    val cCents = cost.toCents() ?: 0L
    val profitCents = pCents - cCents
    val marginPercent = if (pCents > 0) (profitCents.toDouble() / pCents * 100).toInt() else 0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .padding(12.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initial == null) "New Stock Item" else "Edit Stock Item",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LightBlueOnContainer
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SquareField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Item Name"
                    )
                    SquareField(
                        value = qty,
                        onValueChange = { qty = it.filter(Char::isDigit) },
                        label = "In Stock (Units)",
                        keyboardType = KeyboardType.Number
                    )
                    SquareField(
                        value = low,
                        onValueChange = { low = it.filter(Char::isDigit) },
                        label = "Low Stock Warning Threshold",
                        keyboardType = KeyboardType.Number
                    )
                    SquareField(
                        value = price,
                        onValueChange = { price = it },
                        label = "Selling Price (R)",
                        keyboardType = KeyboardType.Decimal
                    )
                    SquareField(
                        value = cost,
                        onValueChange = { cost = it },
                        label = "Cost Price (R)",
                        keyboardType = KeyboardType.Decimal
                    )

                    if (pCents > 0 && cCents > 0) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = LightBlueSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Estimated Profit: ${profitCents.rands()} per unit ($marginPercent% Margin)",
                                style = MaterialTheme.typography.labelLarge,
                                color = LightBluePrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    SquareField(
                        value = daysToExpiry,
                        onValueChange = { daysToExpiry = it.filter(Char::isDigit) },
                        label = "Expiry in Days (0 = None)",
                        keyboardType = KeyboardType.Number
                    )

                    if (initial != null) {
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorContainerLight,
                                contentColor = OnErrorContainerLight
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Stock Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBlueContainer,
                            contentColor = LightBlueOnContainer
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
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
                        },
                        enabled = name.isNotBlank() && price.toCents() != null,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LightBluePrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Save Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
