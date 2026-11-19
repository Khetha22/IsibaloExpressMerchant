package za.co.hbtechinc.isibalo.ui.sales

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import za.co.hbtechinc.isibalo.AppViewModelProvider
import za.co.hbtechinc.isibalo.data.MethodTotal
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.Sale
import za.co.hbtechinc.isibalo.util.rands
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Consistent Light Blue palette matching Assistant, Dashboard, Jobs, Prices, Reports, Settings, AI Setup, Cash-Up, POS, and Expense screens
private val LightBlueSurface = Color(0xFFF0F4F9)
private val LightBlueContainer = Color(0xFFD3E3FD)
private val LightBlueOnContainer = Color(0xFF041E49)
private val LightBluePrimary = Color(0xFF0B57D0)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesScreen(vm: SalesViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val sales by vm.sales.collectAsStateWithLifecycle()
    val totals by vm.totals.collectAsStateWithLifecycle()
    val pending by vm.pending.collectAsStateWithLifecycle()
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val fmt = SimpleDateFormat("HH:mm", Locale.ROOT)
    val context = LocalContext.current
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.syncMessage.collect { snack.showSnackbar(it) }
    }

    // Detect orientation for adaptive landscape/portrait split layout
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBlueSurface
    ) {
        Box(Modifier.fillMaxSize()) {
            if (isLandscape) {
                // Adaptive Two-Column Split for Landscape Mode
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Search, Hero Banner, Payment Method Totals & Sync Status
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        SalesSearchBar(query = query, onQueryChanged = { vm.searchQuery.value = it })
                        SalesHeroCard(sales = sales)
                        PaymentMethodsGrid(totals = totals)
                        SyncStatusCard(pending = pending, onSyncNow = vm::syncNow)
                    }

                    // Right Column: Sales Feed
                    LazyColumn(
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "sales_header_landscape") {
                            Text(
                                text = "Today's Sales Feed (${sales.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = LightBluePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (sales.isEmpty()) {
                            item(key = "empty_sales_landscape") {
                                Text(
                                    "No sales matching your search.",
                                    color = LightBlueOnContainer.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            items(sales, key = { it.id }) { s ->
                                SaleItemCard(sale = s, fmt = fmt, context = context, vm = vm)
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
                    item(key = "search_bar") {
                        SalesSearchBar(query = query, onQueryChanged = { vm.searchQuery.value = it })
                    }

                    item(key = "sales_hero") {
                        SalesHeroCard(sales = sales)
                    }

                    item(key = "payment_methods") {
                        PaymentMethodsGrid(totals = totals)
                    }

                    item(key = "sync_status") {
                        SyncStatusCard(pending = pending, onSyncNow = vm::syncNow)
                    }

                    item(key = "sales_header") {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Today's Sales Feed (${sales.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = LightBluePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (sales.isEmpty()) {
                        item(key = "empty_sales") {
                            Text(
                                "No sales matching your search.",
                                color = LightBlueOnContainer.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        items(sales, key = { it.id }) { s ->
                            SaleItemCard(sale = s, fmt = fmt, context = context, vm = vm)
                        }
                    }
                }
            }
            SnackbarHost(snack, Modifier.align(Alignment.TopCenter))
        }
    }
}

@Composable
private fun SalesSearchBar(query: String, onQueryChanged: (String) -> Unit) {
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
            placeholder = {
                Text(
                    text = "Search sales (method, amount)...",
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
private fun SalesHeroCard(sales: List<Sale>) {
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
                        Icon(
                            imageVector = Icons.Rounded.Payments,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = "Today's Revenue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = sales.sumOf { it.totalCents }.rands(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "${sales.size} transactions recorded today",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentMethodsGrid(totals: List<MethodTotal>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        PaymentMethod.entries.forEach { m ->
            Box(Modifier.fillMaxWidth(0.48f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = m.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = LightBlueOnContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = (totals.firstOrNull { it.method == m }?.total ?: 0L).rands(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LightBluePrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusCard(pending: Int, onSyncNow: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = LightBlueContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = LightBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Cloud Synchronization", fontWeight = FontWeight.Bold, color = LightBlueOnContainer)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (pending == 0) "All sales backed up" else "$pending sales waiting to sync",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = onSyncNow,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBluePrimary, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Sync", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SaleItemCard(
    sale: Sale,
    fmt: SimpleDateFormat,
    context: android.content.Context,
    vm: SalesViewModel
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "sale_item_scale"
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
                    text = sale.totalCents.rands(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = LightBlueOnContainer
                )
                Text(
                    text = "${fmt.format(Date(sale.createdAt))}  •  ${sale.method.label}  •  ${if (sale.synced) "Synced" else "Pending"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = LightBlueOnContainer.copy(alpha = 0.7f)
                )
            }
            Button(
                onClick = {
                    vm.logWhatsAppShare("Sales Receipt", "Receipt of ${sale.totalCents.rands()} (${sale.method.label})")
                    val receiptText = "🧾 *Isibalo Express Receipt*\nTotal: ${sale.totalCents.rands()}\nMethod: ${sale.method.label}\nTime: ${fmt.format(Date(sale.createdAt))}\nThank you for shopping with us!"
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(receiptText)}")
                    }
                    try { context.startActivity(intent) } catch (_: Exception) {}
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightBlueContainer, contentColor = LightBlueOnContainer),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("WhatsApp", fontWeight = FontWeight.Bold)
            }
        }
    }
}
