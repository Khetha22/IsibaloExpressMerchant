package za.co.hbtechinc.isibalo.ui.sales

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.sync.SyncScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SalesViewModel(private val app: Application, repo: Repository) : ViewModel() {
    private val wl = SharingStarted.WhileSubscribed(5000)
    val sales = repo.todaySales().stateIn(viewModelScope, wl, emptyList())
    val totals = repo.todayTotals().stateIn(viewModelScope, wl, emptyList())
    val pending = repo.pendingSales().stateIn(viewModelScope, wl, 0)
    fun syncNow() = SyncScheduler.syncNow(app)
}
