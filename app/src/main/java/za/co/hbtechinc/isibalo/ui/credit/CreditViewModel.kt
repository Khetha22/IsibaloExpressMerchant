package za.co.hbtechinc.isibalo.ui.credit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.CustomerDebt
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CreditViewModel(private val repo: Repository) : ViewModel() {
    val debts: StateFlow<List<CustomerDebt>> = repo.customerDebts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDebt(name: String, phone: String, amountStr: String, note: String) {
        val cents = amountStr.toCents() ?: return
        if (name.isBlank() || cents <= 0) return
        viewModelScope.launch {
            repo.saveDebt(CustomerDebt(customerName = name.trim(), phone = phone.trim(), amountCents = cents, note = note.trim()))
        }
    }

    fun settle(id: Long) {
        viewModelScope.launch { repo.settleDebt(id) }
    }

    fun delete(debt: CustomerDebt) {
        viewModelScope.launch { repo.deleteDebt(debt) }
    }
}
