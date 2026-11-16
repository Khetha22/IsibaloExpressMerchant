package za.co.hbtechinc.isibalo.ui.dashboard

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.MethodTotal
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.Sale
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.sync.SyncScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class OwnerDashboardState(
    val todaySales: List<Sale> = emptyList(),
    val paymentTotals: List<MethodTotal> = emptyList(),
    val stockItems: List<StockItem> = emptyList(),
    val pendingSyncCount: Int = 0,
    val todayExpenses: Long = 0
) {
    val totalRevenue: Long get() = todaySales.sumOf { it.totalCents }
    val netProfit: Long get() = totalRevenue - todayExpenses
    val lowStockCount: Int get() = stockItems.count { it.isLow }
    val expiredCount: Int get() = stockItems.count { it.isExpired || it.isExpiringSoon }
}

class OwnerDashboardViewModel(private val app: Application, repo: Repository) : ViewModel() {
    private val wl = SharingStarted.WhileSubscribed(5000)

    val state: StateFlow<OwnerDashboardState> = combine(
        repo.todaySales(),
        repo.todayTotals(),
        repo.stock(),
        repo.pendingSales(),
        repo.todayExpenses()
    ) { sales, totals, stock, pending, expenses ->
        OwnerDashboardState(
            todaySales = sales,
            paymentTotals = totals,
            stockItems = stock,
            pendingSyncCount = pending,
            todayExpenses = expenses ?: 0L
        )
    }.stateIn(viewModelScope, wl, OwnerDashboardState())

    fun syncNow() = SyncScheduler.syncNow(app)
}
