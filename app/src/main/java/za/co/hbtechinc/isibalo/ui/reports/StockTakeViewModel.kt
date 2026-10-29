package za.co.hbtechinc.isibalo.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.StockItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class StockTakeState(
    val items: List<StockItem> = emptyList(),
    val totalUnits: Int = 0,
    val totalCostValueCents: Long = 0,
    val totalRetailValueCents: Long = 0,
    val lowStockCount: Int = 0,
    val expiredCount: Int = 0
)

class StockTakeViewModel(repo: Repository) : ViewModel() {
    val state: StateFlow<StockTakeState> = repo.stock().map { list ->
        StockTakeState(
            items = list,
            totalUnits = list.sumOf { it.quantity },
            totalCostValueCents = list.sumOf { it.costCents * it.quantity },
            totalRetailValueCents = list.sumOf { it.priceCents * it.quantity },
            lowStockCount = list.count { it.isLow },
            expiredCount = list.count { it.isExpired || it.isExpiringSoon }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StockTakeState())
}
