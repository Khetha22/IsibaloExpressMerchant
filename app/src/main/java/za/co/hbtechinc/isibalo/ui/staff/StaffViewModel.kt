package za.co.hbtechinc.isibalo.ui.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.Staff
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StaffViewModel(private val repo: Repository) : ViewModel() {
    val staffList: StateFlow<List<Staff>> = repo.staff()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveStaff(name: String, role: String, wageStr: String, phone: String, existing: Staff?) {
        val wageCents = wageStr.toCents() ?: 0L
        if (name.isBlank() || wageCents <= 0) return
        viewModelScope.launch {
            repo.saveStaff(
                (existing ?: Staff(name = "", dailyWageCents = 0)).copy(
                    name = name.trim(),
                    role = role.ifBlank { "Cashier" },
                    dailyWageCents = wageCents,
                    phone = phone.trim()
                )
            )
        }
    }

    fun deleteStaff(staff: Staff) {
        viewModelScope.launch { repo.deleteStaff(staff) }
    }
}
