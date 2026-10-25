@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package za.co.hbtechinc.isibalo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import za.co.hbtechinc.isibalo.ui.assistant.AssistantScreen
import za.co.hbtechinc.isibalo.ui.pos.PosScreen
import za.co.hbtechinc.isibalo.ui.sales.SalesScreen
import za.co.hbtechinc.isibalo.ui.stock.StockScreen
import za.co.hbtechinc.isibalo.ui.theme.NavBlue
import za.co.hbtechinc.isibalo.ui.theme.NavMuted
import za.co.hbtechinc.isibalo.ui.theme.ThemeMode

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Sell("sell", "Sell", Icons.Filled.PointOfSale),
    Sales("sales", "Sales", Icons.Filled.BarChart),
    Stock("stock", "Stock", Icons.Filled.Inventory2),
    Assistant("assistant", "Assistant", Icons.Filled.AutoAwesome)
}

@Composable
fun MainScaffold(themeMode: ThemeMode, onTheme: (ThemeMode) -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val current = Tab.entries.firstOrNull { it.route == entry?.destination?.route } ?: Tab.Sell

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ISIBALO", style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp, lineHeight = 26.sp))
                        Text("EXPRESS MERCHANT", style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, letterSpacing = 2.sp, lineHeight = 14.sp), color = NavMuted)
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
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == current,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, tab.label) },
                        label = { Text(tab.label) },
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
        NavHost(nav, startDestination = Tab.Sell.route, modifier = Modifier.padding(pad)) {
            composable(Tab.Sell.route) { PosScreen() }
            composable(Tab.Sales.route) { SalesScreen() }
            composable(Tab.Stock.route) { StockScreen() }
            composable(Tab.Assistant.route) { AssistantScreen() }
        }
    }
}
