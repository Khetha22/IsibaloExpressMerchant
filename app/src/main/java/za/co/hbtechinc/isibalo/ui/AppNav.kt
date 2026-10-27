@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package za.co.hbtechinc.isibalo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
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
import za.co.hbtechinc.isibalo.ui.expenses.ExpenseScreen
import za.co.hbtechinc.isibalo.ui.petty_cash.PettyCashScreen
import za.co.hbtechinc.isibalo.ui.pos.PosScreen
import za.co.hbtechinc.isibalo.ui.sales.SalesScreen
import za.co.hbtechinc.isibalo.ui.stock.StockScreen
import za.co.hbtechinc.isibalo.ui.theme.NavBlue
import za.co.hbtechinc.isibalo.ui.theme.NavMuted
import za.co.hbtechinc.isibalo.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private enum class NavDestination(val route: String, val label: String, val icon: ImageVector, val inBottomBar: Boolean = true) {
    Sell("sell", "Point of Sale", Icons.Filled.PointOfSale, true),
    Sales("sales", "Sales Analytics", Icons.Filled.BarChart, true),
    Stock("stock", "Stock & Inventory", Icons.Filled.Inventory2, true),
    CashUp("cashup", "Cash-Up", Icons.Filled.AccountBalanceWallet, true),
    Assistant("assistant", "AI Assistant", Icons.Filled.AutoAwesome, true),
    Credit("credit", "Customer Credit (Tab)", Icons.Filled.People, false),
    Expenses("expenses", "Expenses & Profit", Icons.Filled.ReceiptLong, false),
    PettyCash("pettycash", "Petty Cash", Icons.Filled.Wallet, false),
    AiSetup("aisetup", "AI Models & RAG", Icons.Filled.Psychology, false)
}

@Composable
fun MainScaffold(themeMode: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = NavDestination.entries.firstOrNull { it.route == entry?.destination?.route } ?: NavDestination.Sell

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavBlue)
                        .padding(20.dp)
                ) {
                    Column {
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
                        onClick = {
                            scope.launch { drawerState.close() }
                            nav.navigate(item.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open Navigation Menu", tint = Color.White)
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
        ) { pad ->
            NavHost(nav, startDestination = NavDestination.Sell.route, modifier = Modifier.padding(pad)) {
                composable(NavDestination.Sell.route) { PosScreen() }
                composable(NavDestination.Sales.route) { SalesScreen() }
                composable(NavDestination.Stock.route) { StockScreen() }
                composable(NavDestination.CashUp.route) { CashUpScreen() }
                composable(NavDestination.Assistant.route) { AssistantScreen() }
                composable(NavDestination.Credit.route) { CreditScreen() }
                composable(NavDestination.Expenses.route) { ExpenseScreen() }
                composable(NavDestination.PettyCash.route) { PettyCashScreen() }
                composable(NavDestination.AiSetup.route) { AiSetupScreen() }
            }
        }
    }
}
