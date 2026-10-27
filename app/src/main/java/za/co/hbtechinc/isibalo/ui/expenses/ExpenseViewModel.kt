package za.co.hbtechinc.isibalo.ui.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Expense
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repo: Repository) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repo.expenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(title: String, amountStr: String, category: String) {
        val cents = amountStr.toCents() ?: return
        if (title.isBlank() || cents <= 0) return
        viewModelScope.launch {
            repo.saveExpense(Expense(title = title.trim(), amountCents = cents, category = category.trim()))
        }
    }

    fun delete(expense: Expense) {
        viewModelScope.launch { repo.deleteExpense(expense) }
    }
}
