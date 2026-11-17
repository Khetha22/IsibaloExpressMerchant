package za.co.hbtechinc.isibalo.ui.unified

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val BrandBlue = Color(0xFF0D47A1)
val BrandBlueLight = Color(0xFFE3F2FD)
val SurfaceBg = Color(0xFFF5F7FA)

enum class AppTab(val label: String, val headerTitle: String) {
    DASHBOARD("Dashboard", "SALES ANALYTICS"),
    POS("POS", "POINT OF SALE"),
    INVENTORY("Stock", "STOCK & INVENTORY"),
    ASSISTANT("AI", "AI ASSISTANT")
}

enum class OrderType(val label: String) {
    DINE_IN("Dine-in"),
    TAKEAWAY("Takeaway"),
    DELIVERY("Delivery")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD_READER("Card Reader"),
    SCAN_QR("Scan to Pay (QR)"),
    MOBILE_MONEY("Mobile Money"),
    DIGITAL_LINK("Digital Link")
}

data class ProductItem(
    val id: String,
    val name: String,
    val price: Double,
    val profit: Double,
    val stock: Int,
    val unitLabel: String = "unit or kg"
)

data class SaleRecord(
    val id: String,
    val amount: Double,
    val time: String,
    val method: PaymentMethod,
    val isPendingSync: Boolean = true
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class IsibaloAppState {
    var currentTab by mutableStateOf(AppTab.POS)
    var selectedOrderType by mutableStateOf(OrderType.DINE_IN)

    val products = mutableStateListOf(
        ProductItem("1", "80M X 80M THERMAL", 610.00, 160.00, 0),
        ProductItem("2", "SPEEDPOINT PAPER", 330.00, 160.00, 0),
        ProductItem("3", "KOTA SPECIAL", 45.00, 20.00, 14, "unit"),
        ProductItem("4", "2L COLDRINK ASSORTED", 28.00, 8.00, 24, "unit"),
        ProductItem("5", "BOEREWORS ROLL", 35.00, 15.00, 9, "unit"),
        ProductItem("6", "FRESH TOMATOES (1KG)", 22.00, 7.00, 18, "kg")
    )

    val cart = mutableStateListOf<ProductItem>()

    val sales = mutableStateListOf(
        SaleRecord("s1", 1880.00, "12:35", PaymentMethod.CASH, true),
        SaleRecord("s2", 30.00, "01:58", PaymentMethod.DIGITAL_LINK, true)
    )

    var pendingSyncCount by mutableStateOf(5)

    val messages = mutableStateListOf(
        ChatMessage(
            "Sawubona! Ask about today's sales, stock or pricing. I run fully offline on this phone.",
            isUser = false
        )
    )

    val cartTotal: Double
        get() = cart.sumOf { it.price }

    val todaySalesTotal: Double
        get() = sales.sumOf { it.amount }

    fun addToCart(product: ProductItem) {
        val currentInCart = cart.count { it.id == product.id }
        if (product.stock > currentInCart) {
            cart.add(product)
        }
    }

    fun updateStock(productId: String, delta: Int) {
        val index = products.indexOfFirst { it.id == productId }
        if (index != -1) {
            val item = products[index]
            val newStock = (item.stock + delta).coerceAtLeast(0)
            products[index] = item.copy(stock = newStock)
        }
    }

    fun completeCharge(method: PaymentMethod) {
        val total = cartTotal
        if (total <= 0.0) return

        cart.groupBy { it.id }.forEach { (id, items) ->
            updateStock(id, -items.size)
        }

        sales.add(
            0,
            SaleRecord(
                id = "s_${System.currentTimeMillis()}",
                amount = total,
                time = "Now",
                method = method,
                isPendingSync = true
            )
        )
        pendingSyncCount += 1
        cart.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsibaloExpressApp(state: IsibaloAppState = remember { IsibaloAppState() }) {
    var isCheckoutOpen by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var globalSearchQuery by remember { mutableStateOf("") }
    
    // Collapsible TopAppBar Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = { isSearchActive = false; globalSearchQuery = "" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = { /* Open Drawer */ }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                },
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = globalSearchQuery,
                            onValueChange = { globalSearchQuery = it },
                            placeholder = { Text("Search across Isibalo...", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column {
                            Text(
                                text = state.currentTab.headerTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "ISIBALO EXPRESS",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Global Search", tint = Color.White)
                        }
                    } else if (globalSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { globalSearchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!isSearchActive) {
                NavigationBar(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                ) {
                    AppTab.entries.forEach { tab ->
                        val selected = state.currentTab == tab
                        val icon = when (tab) {
                            AppTab.DASHBOARD -> Icons.Default.Dashboard
                            AppTab.POS -> Icons.Default.PointOfSale
                            AppTab.INVENTORY -> Icons.Default.Inventory2
                            AppTab.ASSISTANT -> Icons.Default.AutoAwesome
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = { state.currentTab = tab },
                            icon = { Icon(icon, contentDescription = tab.label, modifier = Modifier.size(24.dp)) },
                            label = {
                                Text(
                                    text = tab.label,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandBlue,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.75f),
                                unselectedTextColor = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSearchActive && globalSearchQuery.isNotBlank()) {
                val matchedProducts = state.products.filter { it.name.contains(globalSearchQuery, ignoreCase = true) }
                val matchedSales = state.sales.filter { it.method.label.contains(globalSearchQuery, ignoreCase = true) || it.amount.toString().contains(globalSearchQuery) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(SurfaceBg).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Text("Global Search Results for \"$globalSearchQuery\"", fontWeight = FontWeight.Bold, color = BrandBlue) }
                    
                    item { Text("Products (${matchedProducts.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    items(matchedProducts) { p ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(p.name, fontWeight = FontWeight.Bold)
                                Text("R${"%.2f".format(p.price)}")
                            }
                        }
                    }

                    item { Text("Sales (${matchedSales.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp)) }
                    items(matchedSales) { s ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Sale #${s.id} • ${s.method.label}")
                                Text("R${"%.2f".format(s.amount)}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                when (state.currentTab) {
                    AppTab.DASHBOARD -> SalesAnalyticsScreen(state)
                    AppTab.POS -> PointOfSaleScreen(state, onOpenCheckout = { isCheckoutOpen = true })
                    AppTab.INVENTORY -> StockInventoryScreen(state)
                    AppTab.ASSISTANT -> AiAssistantScreen(state)
                }
            }
        }
    }
}

// 1. SALES ANALYTICS (DASHBOARD)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesAnalyticsScreen(state: IsibaloAppState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BrandBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Today", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Text(
                        text = "R${"%,.2f".format(state.todaySalesTotal)}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                    Text(
                        text = "${state.sales.size} sales recorded",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                }
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentMethod.entries.forEach { method ->
                    val methodTotal = state.sales.filter { it.method == method }.sumOf { it.amount }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(method.label, fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "R${"%,.2f".format(methodTotal)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state.pendingSyncCount > 0)
                            "${state.pendingSyncCount} sales waiting to sync"
                        else "All sales synced",
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = { state.pendingSyncCount = 0 },
                        enabled = state.pendingSyncCount > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                    ) {
                        Text("SYNC NOW")
                    }
                }
            }
        }

        items(state.sales, key = { it.id }) { sale ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "R${"%,.2f".format(sale.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${sale.time} • ${sale.method.label}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    OutlinedButton(onClick = { /* Trigger WhatsApp Intent */ }) {
                        Text("WHATSAPP", color = BrandBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 2. POINT OF SALE SCREEN (FULLY SCROLLABLE IN PORTRAIT & LANDSCAPE)
@Composable
fun PointOfSaleScreen(state: IsibaloAppState, onOpenCheckout: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OrderType.entries.forEach { type ->
                    val selected = state.selectedOrderType == type
                    Button(
                        onClick = { state.selectedOrderType = type },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) BrandBlue else Color.White,
                            contentColor = if (selected) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(type.label, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text("Product Catalog & Menu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        }

        items(state.products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                onClick = { state.addToCart(product) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("R${"%.2f".format(product.price)} • Stock: ${product.stock}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Button(
                        onClick = { state.addToCart(product) },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlueLight),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = BrandBlue)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Cart: ${state.cart.size} items", fontSize = 12.sp, color = Color.Gray)
                        Text("R${"%,.2f".format(state.cartTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                    Button(
                        onClick = onOpenCheckout,
                        enabled = state.cartTotal > 0.0,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("CHARGE ➔")
                    }
                }
            }
        }
    }
}

// 3. STOCK & INVENTORY SCREEN
@Composable
fun StockInventoryScreen(state: IsibaloAppState) {
    val lowStockItems = state.products.filter { it.stock == 0 }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (lowStockItems.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFDECEA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFB71C1C))
                        Column {
                            Text("Restock soon", fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                            Text(text = lowStockItems.joinToString { "${it.name} (0)" }, fontSize = 12.sp, color = Color(0xFF7F0000))
                        }
                    }
                }
            }
        }

        items(state.products, key = { it.id }) { product ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = "R${"%.2f".format(product.price)} | Profit: R${"%.2f".format(product.profit)}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        if (product.stock == 0) {
                            Text("LOW STOCK", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledIconButton(
                            onClick = { state.updateStock(product.id, -1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandBlue),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "${product.stock}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (product.stock == 0) MaterialTheme.colorScheme.error else Color.Black
                        )
                        FilledIconButton(
                            onClick = { state.updateStock(product.id, 1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandBlue),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
            }
        }
    }
}

// 4. MODERNIZED AI ASSISTANT SCREEN WITH STOP & CLEAR & BIGGER TEXTFIELD
@Composable
fun AiAssistantScreen(state: IsibaloAppState) {
    var promptInput by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val quickPrompts = listOf(
        "Summarise today",
        "What should I restock?",
        "How can I boost profit?"
    )

    fun sendPrompt(text: String) {
        if (text.isBlank()) return
        isGenerating = true
        state.messages.add(ChatMessage(text, isUser = true))
        val reply = when {
            text.contains("restock", ignoreCase = true) -> {
                val out = state.products.filter { it.stock == 0 }.joinToString { it.name }
                if (out.isEmpty()) "All items are currently stocked!" else "Priority restock needed for: $out."
            }
            text.contains("Summarise", ignoreCase = true) ->
                "Today's revenue is R${"%,.2f".format(state.todaySalesTotal)} across ${state.sales.size} sales."
            else -> "Offline insight: Bundling Kota Special with 2L Coldrink increases average basket margin by 18%."
        }
        state.messages.add(ChatMessage(reply, isUser = false))
        promptInput = ""
        isGenerating = false
    }

    Column(modifier = Modifier.fillMaxSize().background(SurfaceBg)) {
        // Modern AI Engine Header Pill + Control Buttons (Clear & Stop)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = BrandBlue, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text("Offline Business Advisor", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(if (isGenerating) "Generating response..." else "Gemma 3 1B On-Device Model", fontSize = 11.sp, color = if (isGenerating) BrandBlue else Color.Gray)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { state.messages.clear() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = Color.Gray)
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.messages) { msg ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        color = if (msg.isUser) BrandBlue else Color.White,
                        shape = RoundedCornerShape(18.dp),
                        shadowElevation = 1.dp
                    ) {
                        Text(
                            text = msg.text,
                            modifier = Modifier.padding(14.dp),
                            color = if (msg.isUser) Color.White else Color(0xFF1E293B),
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Quick Prompt Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { sendPrompt(prompt) },
                    label = { Text(prompt, color = BrandBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Bigger, Modern Capsule Composer Bar (Spacious multi-line text field)
        Surface(
            color = Color.White,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                    placeholder = { Text("Ask your AI advisor or speak out loud...", fontSize = 13.sp) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* Voice mic input */ },
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(BrandBlueLight)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice", tint = BrandBlue)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { state.messages.clear() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CLEAR", color = Color.Gray, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { sendPrompt(promptInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(12.dp),
                            enabled = promptInput.isNotBlank() && !isGenerating
                        ) {
                            Text("ASK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
