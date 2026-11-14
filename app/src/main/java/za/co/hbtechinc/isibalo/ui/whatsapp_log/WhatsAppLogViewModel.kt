package za.co.hbtechinc.isibalo.ui.whatsapp_log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.WhatsAppShareLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WhatsAppLogViewModel(private val repo: Repository) : ViewModel() {
    val logs: StateFlow<List<WhatsAppShareLog>> = repo.whatsappLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearLogs() {
        viewModelScope.launch {
            // Delete all logs if requested
            logs.value.forEach { repo.deleteWhatsAppLog(it) }
        }
    }
}
