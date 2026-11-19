package za.co.hbtechinc.isibalo.desktop.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import za.co.hbtechinc.isibalo.desktop.util.rands
import za.co.hbtechinc.isibalo.desktop.util.startOfDay

class DesktopRepository(private val db: DesktopDatabase) {

    private val _stock = MutableStateFlow<List<StockItem>>(emptyList())
    val stock: StateFlow<List<StockItem>> = _stock.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleWithItems>>(emptyList())
    val sales: StateFlow<List<SaleWithItems>> = _sales.asStateFlow()

    private val _cashUp = MutableStateFlow<List<CashUpRecord>>(emptyList())
    val cashUp: StateFlow<List<CashUpRecord>> = _cashUp.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _customerDebts = MutableStateFlow<List<CustomerDebt>>(emptyList())
    val customerDebts: StateFlow<List<CustomerDebt>> = _customerDebts.asStateFlow()

    private val _pettyCash = MutableStateFlow<List<PettyCash>>(emptyList())
    val pettyCash: StateFlow<List<PettyCash>> = _pettyCash.asStateFlow()

    private val _loans = MutableStateFlow<List<MerchantLoan>>(emptyList())
    val loans: StateFlow<List<MerchantLoan>> = _loans.asStateFlow()

    private val _staff = MutableStateFlow<List<Staff>>(emptyList())
    val staff: StateFlow<List<Staff>> = _staff.asStateFlow()

    private val _whatsappLogs = MutableStateFlow<List<WhatsAppShareLog>>(emptyList())
    val whatsappLogs: StateFlow<List<WhatsAppShareLog>> = _whatsappLogs.asStateFlow()

    private val _knowledgeDocs = MutableStateFlow<List<KnowledgeDoc>>(emptyList())
    val knowledgeDocs: StateFlow<List<KnowledgeDoc>> = _knowledgeDocs.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        _stock.value = db.getAllStock()
        _sales.value = db.getSalesSince(startOfDay())
        _cashUp.value = db.getAllCashUp()
        _expenses.value = db.getAllExpenses()
        _customerDebts.value = db.getAllCustomerDebts()
        _pettyCash.value = db.getAllPettyCash()
        _loans.value = db.getAllLoans()
        _staff.value = db.getAllStaff()
        _whatsappLogs.value = db.getAllWhatsAppLogs()
        _knowledgeDocs.value = db.getAllKnowledgeDocs()
    }

    fun saveStock(item: StockItem) {
        db.upsertStock(item)
        refreshAll()
    }

    fun deleteStock(item: StockItem) {
        db.deleteStock(item.id)
        refreshAll()
    }

    fun checkout(lines: List<CartLine>, method: PaymentMethod): Long {
        val saleId = db.checkout(lines, method)
        refreshAll()
        return saleId
    }

    fun saveCashUp(record: CashUpRecord): Long {
        val id = db.insertCashUp(record)
        refreshAll()
        return id
    }

    fun saveExpense(expense: Expense): Long {
        val id = db.insertExpense(expense)
        refreshAll()
        return id
    }

    fun deleteExpense(expense: Expense) {
        db.deleteExpense(expense.id)
        refreshAll()
    }

    fun saveDebt(debt: CustomerDebt): Long {
        val id = db.insertCustomerDebt(debt)
        refreshAll()
        return id
    }

    fun settleDebt(id: Long) {
        db.settleDebt(id)
        refreshAll()
    }

    fun deleteDebt(debt: CustomerDebt) {
        db.deleteDebt(debt.id)
        refreshAll()
    }

    fun savePettyCash(record: PettyCash): Long {
        val id = db.insertPettyCash(record)
        refreshAll()
        return id
    }

    fun settlePettyCash(id: Long) {
        db.settlePettyCash(id)
        refreshAll()
    }

    fun deletePettyCash(record: PettyCash) {
        db.deletePettyCash(record.id)
        refreshAll()
    }

    fun applyForLoan(principalCents: Long): Long {
        val id = db.insertLoan(principalCents)
        refreshAll()
        return id
    }

    fun repayLoan(id: Long, amountCents: Long) {
        db.repayLoan(id, amountCents)
        refreshAll()
    }

    fun saveStaff(staff: Staff) {
        db.upsertStaff(staff)
        refreshAll()
    }

    fun deleteStaff(staff: Staff) {
        db.deleteStaff(staff.id)
        refreshAll()
    }

    fun logWhatsAppShare(title: String, summary: String): Long {
        val id = db.insertWhatsAppLog(title, summary)
        refreshAll()
        return id
    }

    fun deleteWhatsAppLog(log: WhatsAppShareLog) {
        db.deleteWhatsAppLog(log.id)
        refreshAll()
    }

    fun saveKnowledgeDoc(doc: KnowledgeDoc) {
        db.upsertKnowledgeDoc(doc)
        refreshAll()
    }

    fun businessSnapshot(): String {
        val currentSales = _sales.value
        val totalRev = currentSales.sumOf { it.sale.totalCents }
        val lowStock = _stock.value.filter { it.isLow }
        val debts = _customerDebts.value.filter { !it.settled }
        val totalDebts = debts.sumOf { it.amountCents }

        return buildString {
            appendLine("Sales today: ${currentSales.size}, total ${totalRev.rands()}")
            appendLine("Low stock items: ${if (lowStock.isEmpty()) "None" else lowStock.joinToString { "${it.name} (${it.quantity})" }}")
            appendLine("Outstanding customer debts: ${debts.size} tabs, total ${totalDebts.rands()}")
            appendLine("Total inventory SKUs: ${_stock.value.size}")
        }
    }
}
