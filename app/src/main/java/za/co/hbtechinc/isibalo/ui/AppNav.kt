@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package za.co.hbtechinc.isibalo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import za.co.hbtechinc.isibalo.ui.ai_setup.AiSetupScreen
import za.co.hbtechinc.isibalo.ui.assistant.AssistantScreen
import za.co.hbtechinc.isibalo.ui.cashup.CashUpScreen
import za.co.hbtechinc.isibalo.ui.credit.CreditScreen
import za.co.hbtechinc.isibalo.ui.dashboard.OwnerDashboardScreen
import za.co.hbtechinc.isibalo.ui.expenses.ExpenseScreen
import za.co.hbtechinc.isibalo.ui.loans.LoanScreen
import za.co.hbtechinc.isibalo.ui.media.MediaSearchScreen
import za.co.hbtechinc.isibalo.ui.petty_cash.PettyCashScreen
import za.co.hbtechinc.isibalo.ui.pos.PosScreen
import za.co.hbtechinc.isibalo.ui.reports.FinancialReportScreen
import za.co.hbtechinc.isibalo.ui.reports.StockTakeScreen
import za.co.hbtechinc.isibalo.ui.sales.SalesScreen
import za.co.hbtechinc.isibalo.ui.staff.StaffScreen
import za.co.hbtechinc.isibalo.ui.statements.FinancialStatementScreen
import za.co.hbtechinc.isibalo.ui.stock.StockScreen
import za.co.hbtechinc.isibalo.ui.theme.NavBlue
import za.co.hbtechinc.isibalo.ui.theme.NavMuted
import za.co.hbtechinc.isibalo.ui.theme.ThemeMode
import za.co.hbtechinc.isibalo.ui.whatsapp_log.WhatsAppLogScreen
import kotlinx.coroutines.launch

private enum class NavDestination(val route: String, val label: String, val icon: ImageVector, val inBottomBar: Boolean = true) {
    OwnerDashboard("ownerdashboard", "Dashboard", Icons.Filled.Dashboard, true),
    Sell("sell", "Point of Sale", Icons.Filled.PointOfSale, true),
    Stock("stock", "Stock & Inventory", Icons.Filled.Inventory2, true),
    Assistant("assistant", "AI Assistant", Icons.Filled.AutoAwesome, true),
    Sales("sales", "Sales Analytics", Icons.Filled.BarChart, false),
    CashUp("cashup", "Cash-Up", Icons.Filled.AccountBalanceWallet, false),
    Statements("statements", "PDF Financial Statements", Icons.Filled.Description, false),
    Credit("credit", "Customer Credit (Tab)", Icons.Filled.People, false),
    Expenses("expenses", "Expenses & Profit", Icons.Filled.ReceiptLong, false),
    PettyCash("pettycash", "Petty Cash", Icons.Filled.Wallet, false),
    Loans("loans", "Working Capital Advances", Icons.Filled.RequestQuote, false),
    Staff("staff", "Staff & Payroll", Icons.Filled.Group, false),
    FinancialReport("financialreport", "Financial Report", Icons.Filled.Assessment, false),
    StockTake("stocktake", "Stock Take Audit", Icons.Filled.Inventory, false),
    WhatsAppLog("whatsapplog", "WhatsApp Share Activity", Icons.Filled.Share, false),
    MediaSearch("mediasearch", "Media & Video Search", Icons.Filled.VideoLibrary, false),
    AiSetup("aisetup", "AI Models & RAG", Icons.Filled.Psychology, false)
}

@Composable
private fun AppRailContent(
    current: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onExpand: () -> Unit
) {
    NavigationRail(
        containerColor = NavBlue,
        contentColor = Color.White,
        header = {
            IconButton(onClick = onExpand, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Filled.Menu, contentDescription = "Expand Menu", tint = Color.White)
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavDestination.entries.forEach { item ->
                NavigationRailItem(
                    selected = item == current,
                    onClick = { onNavigate(item) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label.take(6) + "..", fontSize = 9.sp) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = NavBlue,
                        selectedTextColor = Color.White,
                        indicatorColor = Color.White,
                        unselectedIconColor = NavMuted,
                        unselectedTextColor = NavMuted
                    )
                )
            }
        }
    }
}

@Composable
private fun AppDrawerContent(
    current: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    onCollapse: (() -> Unit)? = null
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavBlue)
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ISIBALO",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 32.sp, lineHeight = 30.sp),
                                color = Color.White
                            )
                            Text(
                                "EXPRESS MERCHANT",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, letterSpacing = 2.sp),
                                color = NavMuted
                            )
                        }
                        if (onCollapse != null) {
                            IconButton(onClick = onCollapse) {
                                Icon(Icons.Filled.ChevronLeft, contentDescription = "Collapse Menu", tint = Color.White)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "On-The-Go Business OS • Offline-First",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            NavDestination.entries.forEach { item ->
                NavigationDrawerItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    selected = current == item,
                    onClick = { onNavigate(item) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun MainScaffold(themeMode: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = NavDestination.entries.firstOrNull { it.route == entry?.destination?.route } ?: NavDestination.OwnerDashboard

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isRailExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWideScreen = this.maxWidth >= 560.dp || this.maxWidth > this.maxHeight

        val scaffoldContent = @Composable {
            Scaffold(
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            if (!isWideScreen) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Menu", tint = Color.White)
                                }
                            }
                        },
                        title = {
                            Column {
                                Text(current.label, style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp), color = Color.White)
                                Text("ISIBALO EXPRESS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = NavMuted)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = NavBlue, titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        ),
                        actions = {
                            IconButton(onClick = {
                                onTheme(when (themeMode) {
                                    ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                    ThemeMode.LIGHT -> ThemeMode.DARK
                                    ThemeMode.DARK -> ThemeMode.SYSTEM
                                })
                            }) {
                                Icon(
                                    when (themeMode) {
                                        ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto
                                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                                        ThemeMode.DARK -> Icons.Filled.DarkMode
                                    },
                                    contentDescription = "Theme: ${themeMode.name.lowercase()}"
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(containerColor = NavBlue, contentColor = Color.White) {
                            NavDestination.entries.filter { it.inBottomBar }.forEach { item ->
                                NavigationBarItem(
                                    selected = item == current,
                                    onClick = {
                                        nav.navigate(item.route) {
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(item.icon, item.label) },
                                    label = { Text(item.label, fontSize = 11.sp) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NavBlue, selectedTextColor = Color.White,
                                        indicatorColor = Color.White,
                                        unselectedIconColor = NavMuted, unselectedTextColor = NavMuted
                                    )
                                )
                            }
                        }
                    }
                }
            ) { pad ->
                NavHost(nav, startDestination = NavDestination.OwnerDashboard.route, modifier = Modifier.padding(pad)) {
                    composable(NavDestination.OwnerDashboard.route) { OwnerDashboardScreen() }
                    composable(NavDestination.Sell.route) { PosScreen() }
                    composable(NavDestination.Sales.route) { SalesScreen() }
                    composable(NavDestination.Stock.route) { StockScreen() }
                    composable(NavDestination.CashUp.route) { CashUpScreen() }
                    composable(NavDestination.Assistant.route) { AssistantScreen() }
                    composable(NavDestination.Statements.route) { FinancialStatementScreen() }
                    composable(NavDestination.Credit.route) { CreditScreen() }
                    composable(NavDestination.Expenses.route) { ExpenseScreen() }
                    composable(NavDestination.PettyCash.route) { PettyCashScreen() }
                    composable(NavDestination.Loans.route) { LoanScreen() }
                    composable(NavDestination.Staff.route) { StaffScreen() }
                    composable(NavDestination.FinancialReport.route) { FinancialReportScreen() }
                    composable(NavDestination.StockTake.route) { StockTakeScreen() }
                    composable(NavDestination.WhatsAppLog.route) { WhatsAppLogScreen() }
                    composable(NavDestination.MediaSearch.route) { MediaSearchScreen() }
                    composable(NavDestination.AiSetup.route) { AiSetupScreen() }
                }
            }
        }

        if (isWideScreen) {
            Row(Modifier.fillMaxSize()) {
                if (isRailExpanded) {
                    AppDrawerContent(
                        current = current,
                        onNavigate = { item ->
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onCollapse = { isRailExpanded = false }
                    )
                } else {
                    AppRailContent(
                        current = current,
                        onNavigate = { item ->
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onExpand = { isRailExpanded = true }
                    )
                }
                Box(Modifier.weight(1f)) {
                    scaffoldContent()
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AppDrawerContent(
                        current = current,
                        onNavigate = { item ->
                            scope.launch { drawerState.close() }
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) {
                scaffoldContent()
            }
        }
    }
}
