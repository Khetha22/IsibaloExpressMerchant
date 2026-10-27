package za.co.hbtechinc.isibalo.ui.cashup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import za.co.hbtechinc.isibalo.data.CashUpRecord
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.util.rands
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CashUpUiState(
    val floatText: String = "500.00",
    val actualCashText: String = "",
    val notesText: String = "",
    val expectedCashCents: Long = 0,
    val cardTotalCents: Long = 0,
    val mobileTotalCents: Long = 0,
    val isSavedSuccess: Boolean = false,
    val error: String? = null
) {
    val floatCents: Long get() = (floatText.toDoubleOrNull() ?: 0.0).times(100).toLong()
    val actualCashCents: Long get() = (actualCashText.toDoubleOrNull() ?: 0.0).times(100).toLong()
    val varianceCents: Long get() = actualCashCents - expectedCashCents
}

class CashUpViewModel(private val repo: Repository) : ViewModel() {

    private val _uiState = MutableStateFlow(CashUpUiState())
    val uiState: StateFlow<CashUpUiState> = _uiState.asStateFlow()

    val cashUpHistory: StateFlow<List<CashUpRecord>> = repo.cashUpRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repo.todayTotals().collect { totals ->
                val cash = totals.firstOrNull { it.method == PaymentMethod.CASH }?.total ?: 0L
                val card = totals.firstOrNull { it.method == PaymentMethod.CARD }?.total ?: 0L
                val mobile = totals.firstOrNull { it.method == PaymentMethod.MOBILE }?.total ?: 0L

                _uiState.value = _uiState.value.copy(
                    expectedCashCents = cash,
                    cardTotalCents = card,
                    mobileTotalCents = mobile
                )
            }
        }
    }

    fun onFloatChanged(v: String) { _uiState.value = _uiState.value.copy(floatText = v, isSavedSuccess = false) }
    fun onActualCashChanged(v: String) { _uiState.value = _uiState.value.copy(actualCashText = v, isSavedSuccess = false) }
    fun onNotesChanged(v: String) { _uiState.value = _uiState.value.copy(notesText = v, isSavedSuccess = false) }

    fun saveCashUp() {
        val s = _uiState.value
        val record = CashUpRecord(
            floatCents = s.floatCents,
            expectedCashCents = s.expectedCashCents,
            actualCashCents = s.actualCashCents,
            varianceCents = s.varianceCents,
            cardTotalCents = s.cardTotalCents,
            mobileTotalCents = s.mobileTotalCents,
            notes = s.notesText
        )
        viewModelScope.launch {
            repo.saveCashUp(record)
            _uiState.value = _uiState.value.copy(
                isSavedSuccess = true,
                actualCashText = "",
                notesText = ""
            )
        }
    }
}
