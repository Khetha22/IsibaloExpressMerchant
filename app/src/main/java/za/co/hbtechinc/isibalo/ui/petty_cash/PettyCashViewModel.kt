package za.co.hbtechinc.isibalo.ui.petty_cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.PettyCash
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PettyCashViewModel(private val repo: Repository) : ViewModel() {
    val records: StateFlow<List<PettyCash>> = repo.pettyCashRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun issuePettyCash(recipient: String, amountStr: String, note: String) {
        val cents = amountStr.toCents() ?: return
        if (recipient.isBlank() || cents <= 0) return
        viewModelScope.launch {
            repo.savePettyCash(PettyCash(recipient = recipient.trim(), amountCents = cents, note = note.trim()))
        }
    }

    fun settle(id: Long) {
        viewModelScope.launch { repo.settlePettyCash(id) }
    }

    fun delete(record: PettyCash) {
        viewModelScope.launch { repo.deletePettyCash(record) }
    }
}
