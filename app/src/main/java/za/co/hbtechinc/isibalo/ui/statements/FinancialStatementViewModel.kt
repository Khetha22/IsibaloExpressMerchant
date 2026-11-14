package za.co.hbtechinc.isibalo.ui.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.MethodTotal
import za.co.hbtechinc.isibalo.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatementState(
    val totalRevenue: Long = 0,
    val totalExpenses: Long = 0,
    val stockValuation: Long = 0,
    val outstandingTabs: Long = 0,
    val paymentTotals: List<MethodTotal> = emptyList()
) {
    val netProfit: Long get() = totalRevenue - totalExpenses
    val totalAssetBase: Long get() = totalRevenue + stockValuation
}

class FinancialStatementViewModel(repo: Repository) : ViewModel() {
    private val wl = SharingStarted.WhileSubscribed(5000)

    val state: StateFlow<StatementState> = combine(
        repo.todaySales(),
        repo.todayTotals(),
        repo.todayExpenses(),
        repo.stock(),
        repo.customerDebts()
    ) { sales, totals, expenses, stock, debts ->
        StatementState(
            totalRevenue = sales.sumOf { it.totalCents },
            totalExpenses = expenses ?: 0L,
            stockValuation = stock.sumOf { it.priceCents * it.quantity },
            outstandingTabs = debts.filter { !it.settled }.sumOf { it.amountCents },
            paymentTotals = totals
        )
    }.stateIn(viewModelScope, wl, StatementState())
}
