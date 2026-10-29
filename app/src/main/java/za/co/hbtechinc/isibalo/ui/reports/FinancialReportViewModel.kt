package za.co.hbtechinc.isibalo.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.MethodTotal
import za.co.hbtechinc.isibalo.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class FinancialReportState(
    val totalRevenue: Long = 0,
    val totalExpenses: Long = 0,
    val paymentTotals: List<MethodTotal> = emptyList()
) {
    val grossProfit: Long get() = totalRevenue // Simplified COGS integration
    val netProfit: Long get() = totalRevenue - totalExpenses
}

class FinancialReportViewModel(repo: Repository) : ViewModel() {
    private val wl = SharingStarted.WhileSubscribed(5000)

    val state: StateFlow<FinancialReportState> = combine(
        repo.todaySales(),
        repo.todayTotals(),
        repo.todayExpenses()
    ) { sales, totals, expenses ->
        FinancialReportState(
            totalRevenue = sales.sumOf { it.totalCents },
            totalExpenses = expenses ?: 0L,
            paymentTotals = totals
        )
    }.stateIn(viewModelScope, wl, FinancialReportState())
}
