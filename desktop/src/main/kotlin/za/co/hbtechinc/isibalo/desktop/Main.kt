package za.co.hbtechinc.isibalo.desktop

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import za.co.hbtechinc.isibalo.desktop.data.DesktopDatabase
import za.co.hbtechinc.isibalo.desktop.data.DesktopRepository
import za.co.hbtechinc.isibalo.desktop.ui.DesktopNav
import za.co.hbtechinc.isibalo.desktop.ui.LightBlueContainer
import za.co.hbtechinc.isibalo.desktop.ui.LightBlueOnContainer
import za.co.hbtechinc.isibalo.desktop.ui.LightBluePrimary
import za.co.hbtechinc.isibalo.desktop.ui.LightBlueSurface
import za.co.hbtechinc.isibalo.desktop.ui.screens.*

fun main() = application {
    val database = remember { DesktopDatabase() }
    val repository = remember { DesktopRepository(database) }

    val windowState = rememberWindowState(width = 1400.dp, height = 850.dp)
    Window(
        onCloseRequest = ::exitApplication,
        title = "Isibalo Express Merchant - Desktop OS",
        state = windowState
    ) {
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = LightBlueSurface
            ) {
                var currentNav by remember { mutableStateOf(DesktopNav.Dashboard) }

                Row(modifier = Modifier.fillMaxSize()) {
                    // Desktop Navigation Sidebar
                    Surface(
                        modifier = Modifier.width(280.dp).fillMaxHeight(),
                        color = Color.White,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header Branding
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = LightBluePrimary),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "ISIBALO EXPRESS",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Merchant Desktop OS • SQLite Active",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LightBlueContainer
                                    )
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            DesktopNav.entries.forEach { nav ->
                                val selected = currentNav == nav
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { currentNav = nav },
                                    color = if (selected) LightBlueContainer else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = nav.icon,
                                            contentDescription = nav.title,
                                            tint = if (selected) LightBluePrimary else LightBlueOnContainer.copy(alpha = 0.7f),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Text(
                                            text = nav.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) LightBluePrimary else LightBlueOnContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Main Content Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Header Bar
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentNav.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = LightBlueOnContainer
                                    )
                                    Text(
                                        text = "Offline-First Desktop Operations • SQLite Embedded Engine",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LightBlueOnContainer.copy(alpha = 0.6f)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = LightBlueContainer
                                ) {
                                    Text(
                                        text = "🟢 SQLite Ready (Local Storage)",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LightBluePrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Content Body based on selected navigation
                        Surface(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                when (currentNav) {
                                    DesktopNav.Dashboard -> DashboardDesktopScreen(repository) { currentNav = it }
                                    DesktopNav.POS -> PosDesktopScreen(repository)
                                    DesktopNav.Stock -> StockDesktopScreen(repository)
                                    DesktopNav.Sales -> SalesDesktopScreen(repository)
                                    DesktopNav.CashUp -> CashUpDesktopScreen(repository)
                                    DesktopNav.Expenses -> ExpensesDesktopScreen(repository)
                                    DesktopNav.Credit -> CreditDesktopScreen(repository)
                                    DesktopNav.Staff -> StaffDesktopScreen(repository)
                                    DesktopNav.Reports -> ReportsDesktopScreen(repository)
                                    DesktopNav.Assistant -> AssistantDesktopScreen(repository)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
