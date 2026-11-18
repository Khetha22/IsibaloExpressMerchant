package za.co.hbtechinc.isibalo.ui.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.CartLine
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.util.calculateCommissionCents
import za.co.hbtechinc.isibalo.util.rands
import za.co.hbtechinc.isibalo.util.toCents
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

class PosViewModel(private val repo: Repository) : ViewModel() {

    val searchQuery = MutableStateFlow("")

    val items: StateFlow<List<StockItem>> = combine(repo.stock(), searchQuery) { list, q ->
        val trimmed = q.trim()
        if (trimmed.isBlank()) list
        else list.filter { it.name.contains(trimmed, ignoreCase = true) }
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private val _cart = MutableStateFlow<List<CartLine>>(emptyList())
    val cart: StateFlow<List<CartLine>> = _cart.asStateFlow()

    val quickAmount = MutableStateFlow("")
    val cashTendered = MutableStateFlow("")
    val method = MutableStateFlow(PaymentMethod.CASH)
    val orderType = MutableStateFlow("Dine-in") // Dine-in, Takeaway, Delivery
    val businessType = MutableStateFlow("Retailer") // "Retailer" or "Restaurant"
    val isCheckoutSheetOpen = MutableStateFlow(false)

    val total: StateFlow<Long> = combine(_cart, quickAmount) { c, q ->
        c.sumOf { it.unitPriceCents * it.qty } + (q.toCents() ?: 0L)
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0L
        )

    val changeDue: StateFlow<Long> = combine(total, cashTendered) { t, tendered ->
        val tenderedCents = tendered.toCents() ?: 0L
        if (tenderedCents > t) tenderedCents - t else 0L
    }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0L
        )

    private val _message = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val message = _message.asSharedFlow()

    fun setOrderType(type: String) { orderType.value = type }
    fun setBusinessType(type: String) { businessType.value = type }
    fun setCheckoutSheetOpen(open: Boolean) { isCheckoutSheetOpen.value = open }

    fun add(item: StockItem) {
        if (item.quantity <= 0) {
            _message.tryEmit("${item.name} is out of stock")
            return
        }
        _cart.update { list ->
            val ex = list.find { it.stockItemId == item.id && it.weightKg == null }
            when {
                ex == null -> list + CartLine(item.id, item.name, item.priceCents, 1, item.quantity)
                ex.qty >= ex.maxQty -> list
                else -> list.map { if (it === ex) it.copy(qty = it.qty + 1) else it }
            }
        }
    }

    fun addWeighted(item: StockItem, weightKg: Double) {
        if (weightKg <= 0.0) return
        val calculatedPriceCents = (item.priceCents * weightKg).toLong()
        _cart.update { list ->
            list + CartLine(
                stockItemId = item.id,
                name = "${item.name} (${String.format(Locale.ROOT, "%.2f", weightKg)} kg)",
                unitPriceCents = calculatedPriceCents,
                qty = 1,
                maxQty = item.quantity,
                weightKg = weightKg
            )
        }
    }

    fun updateKitchenNote(line: CartLine, note: String) {
        _cart.update { list ->
            list.map { if (it === line) it.copy(kitchenNote = note.trim()) else it }
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

    fun setQuick(v: String) {
        quickAmount.value = v.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun setCashTendered(v: String) {
        cashTendered.value = v.filter { it.isDigit() || it == '.' || it == ',' }
    }

    fun charge() {
        viewModelScope.launch {
            val quick = quickAmount.value.toCents() ?: 0L
            val lines = _cart.value + if (quick > 0) listOf(CartLine(null, "Quick sale", quick, 1)) else emptyList()
            if (lines.isEmpty()) return@launch

            val finalTotal = lines.sumOf { it.unitPriceCents * it.qty }
            repo.checkout(lines, method.value)

            val m = method.value
            if (m == PaymentMethod.QR_CODE || m == PaymentMethod.GATEWAY || m == PaymentMethod.CARD) {
                val commission = finalTotal.calculateCommissionCents()
                _message.tryEmit("Sale saved via ${m.label}. Platform fee (2.5%): ${commission.rands()} deducted.")
            } else {
                _message.tryEmit("Cash sale saved successfully. Change calculated.")
            }

            _cart.value = emptyList()
            quickAmount.value = ""
            cashTendered.value = ""
            isCheckoutSheetOpen.value = false
        }
    }
}