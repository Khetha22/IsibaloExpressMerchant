package za.co.hbtechinc.isibalo.ui.unified

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import za.co.hbtechinc.isibalo.ui.assistant.AssistantScreen
import za.co.hbtechinc.isibalo.ui.dashboard.OwnerDashboardScreen
import za.co.hbtechinc.isibalo.ui.pos.PosScreen
import za.co.hbtechinc.isibalo.ui.stock.StockScreen
import za.co.hbtechinc.isibalo.ui.theme.NavBlue
import za.co.hbtechinc.isibalo.ui.theme.NavMuted

enum class UnifiedTab(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Filled.Dashboard),
    Pos("Point of Sale", Icons.Filled.PointOfSale),
    Stock("Stock & Inv.", Icons.Filled.Inventory2),
    Ai("AI Assistant", Icons.Filled.AutoAwesome)
}

@Composable
fun UnifiedAppScreen() {
    var selectedTab by remember { mutableStateOf(UnifiedTab.Dashboard) }
    val cs = MaterialTheme.colorScheme

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = NavBlue, contentColor = Color.White) {
                UnifiedTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
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
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
                .background(cs.background)
        ) {
            when (selectedTab) {
                UnifiedTab.Dashboard -> OwnerDashboardScreen()
                UnifiedTab.Pos -> PosScreen()
                UnifiedTab.Stock -> StockScreen()
                UnifiedTab.Ai -> AssistantScreen()
            }
        }
    }
}
