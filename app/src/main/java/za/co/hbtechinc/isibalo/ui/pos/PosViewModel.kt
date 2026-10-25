package za.co.hbtechinc.isibalo.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.CartLine
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PosViewModel(private val repo: Repository) : ViewModel() {

    val items = repo.stock().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart = _cart.asStateFlow()

    val quickAmount = MutableStateFlow("")
    val method = MutableStateFlow(PaymentMethod.CASH)

    val total = combine(_cart, quickAmount) { c, q ->
        c.sumOf { it.unitPriceCents * it.qty } + (q.toCents() ?: 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    fun add(item: StockItem) {
        if (item.quantity <= 0) { _message.tryEmit("${item.name} is out of stock"); return }
        _cart.update { list ->
            val ex = list.find { it.stockItemId == item.id }
            when {
                ex == null -> list + CartLine(item.id, item.name, item.priceCents, 1, item.quantity)
                ex.qty >= ex.maxQty -> list
                else -> list.map { if (it === ex) it.copy(qty = it.qty + 1) else it }
            }
        }
    }

    fun change(line: CartLine, delta: Int) {
        _cart.update { list ->
            list.mapNotNull {
                if (it !== line) it
                else {
                    val q = (it.qty + delta).coerceAtMost(it.maxQty)
                    if (q <= 0) null else it.copy(qty = q)
                }
            }
        }
    }

    fun setQuick(v: String) { quickAmount.value = v.filter { it.isDigit() || it == '.' || it == ',' } }

    fun charge() {
        viewModelScope.launch {
            val quick = quickAmount.value.toCents() ?: 0L
            val lines = _cart.value + if (quick > 0) listOf(CartLine(null, "Quick sale", quick, 1)) else emptyList()
            if (lines.isEmpty()) return@launch
            repo.checkout(lines, method.value)
            _cart.value = emptyList()
            quickAmount.value = ""
            _message.tryEmit("Sale saved")
        }
    }
}
