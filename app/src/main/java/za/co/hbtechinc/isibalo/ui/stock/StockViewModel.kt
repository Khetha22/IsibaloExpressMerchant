package za.co.hbtechinc.isibalo.ui.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.StockItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StockViewModel(private val repo: Repository) : ViewModel() {
    private val wl = SharingStarted.WhileSubscribed(5000)

    val searchQuery = MutableStateFlow("")

    val items = combine(repo.stock(), searchQuery) { list, q ->
        if (q.isBlank()) list
        else list.filter { it.name.contains(q.trim(), ignoreCase = true) }
    }.stateIn(viewModelScope, wl, emptyList())

    val low = repo.lowStock().stateIn(viewModelScope, wl, emptyList())

    fun save(item: StockItem) = viewModelScope.launch { repo.saveStock(item) }
    fun delete(item: StockItem) = viewModelScope.launch { repo.deleteStock(item) }
    fun adjust(item: StockItem, delta: Int) =
        save(item.copy(quantity = (item.quantity + delta).coerceAtLeast(0)))
}
