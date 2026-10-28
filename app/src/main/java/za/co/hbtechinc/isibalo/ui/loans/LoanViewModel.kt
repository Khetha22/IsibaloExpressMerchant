package za.co.hbtechinc.isibalo.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.MerchantLoan
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LoanViewModel(private val repo: Repository) : ViewModel() {
    val loans: StateFlow<List<MerchantLoan>> = repo.merchantLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun applyLoan(amountStr: String) {
        val cents = amountStr.toCents() ?: return
        if (cents <= 0) return
        viewModelScope.launch {
            repo.applyForLoan(cents)
        }
    }

    fun makeRepayment(id: Long, amountStr: String) {
        val cents = amountStr.toCents() ?: return
        if (cents <= 0) return
        viewModelScope.launch {
            repo.repayLoan(id, cents)
        }
    }
}
