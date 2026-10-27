package za.co.hbtechinc.isibalo.data

import androidx.room.withTransaction
import za.co.hbtechinc.isibalo.util.rands
import za.co.hbtechinc.isibalo.util.startOfDay
import kotlinx.coroutines.flow.first

class Repository(private val db: AppDatabase) {
    private val stockDao = db.stockDao()
    private val salesDao = db.salesDao()
    private val cashUpDao = db.cashUpDao()
    private val knowledgeDao = db.knowledgeDao()
    private val debtDao = db.customerDebtDao()
    private val expenseDao = db.expenseDao()
    private val pettyCashDao = db.pettyCashDao()

    fun stock() = stockDao.observeAll()
    fun lowStock() = stockDao.observeLow()
    fun todaySales() = salesDao.observeSince(startOfDay())
    fun todayTotals() = salesDao.observeTotalsSince(startOfDay())
    fun pendingSales() = salesDao.observePending()
    fun cashUpRecords() = cashUpDao.observeAll()
    fun knowledgeDocs() = knowledgeDao.observeAll()
    fun customerDebts() = debtDao.observeAll()
    fun expenses() = expenseDao.observeAll()
    fun todayExpenses() = expenseDao.observeTotalSince(startOfDay())
    fun pettyCashRecords() = pettyCashDao.observeAll()

    suspend fun saveStock(item: StockItem) =
        stockDao.upsert(item.copy(updatedAt = System.currentTimeMillis(), synced = false))

    suspend fun deleteStock(item: StockItem) = stockDao.delete(item)

    suspend fun saveCashUp(record: CashUpRecord) = cashUpDao.insert(record)

    suspend fun saveKnowledgeDoc(doc: KnowledgeDoc) =
        knowledgeDao.upsert(doc.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteKnowledgeDoc(doc: KnowledgeDoc) = knowledgeDao.delete(doc)

    suspend fun allKnowledgeDocs(): List<KnowledgeDoc> = knowledgeDao.getAll()

    suspend fun saveDebt(debt: CustomerDebt) = debtDao.insert(debt)
    suspend fun settleDebt(id: Long) = debtDao.settle(id)
    suspend fun deleteDebt(debt: CustomerDebt) = debtDao.delete(debt)

    suspend fun saveExpense(expense: Expense) = expenseDao.insert(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)

    suspend fun savePettyCash(record: PettyCash) = pettyCashDao.insert(record)
    suspend fun settlePettyCash(id: Long) = pettyCashDao.settle(id)
    suspend fun deletePettyCash(record: PettyCash) = pettyCashDao.delete(record)

    /** Saves the sale and deducts stock in one atomic transaction. Works fully offline. */
    suspend fun checkout(lines: List<CartLine>, method: PaymentMethod): Long = db.withTransaction {
        val now = System.currentTimeMillis()
        val total = lines.sumOf { it.unitPriceCents * it.qty }
        val saleId = salesDao.insertSale(Sale(totalCents = total, method = method, createdAt = now))
        salesDao.insertItems(lines.map {
            SaleItem(saleId = saleId, stockItemId = it.stockItemId, name = it.name,
                qty = it.qty, unitPriceCents = it.unitPriceCents)
        })
        lines.forEach { l -> l.stockItemId?.let { stockDao.decrement(it, l.qty, now) } }
        saleId
    }

    // ---- sync helpers ----
    suspend fun unsyncedSales() = salesDao.unsynced()
    suspend fun unsyncedStock() = stockDao.unsynced()
    suspend fun markSynced(saleIds: List<Long>, stockIds: List<Long>) {
        if (saleIds.isNotEmpty()) salesDao.markSynced(saleIds)
        if (stockIds.isNotEmpty()) stockDao.markSynced(stockIds)
    }

    /** Plain-text summary fed to the on-device AI so answers use real shop data. */
    suspend fun businessSnapshot(): String {
        val from = startOfDay()
        val sales = salesDao.observeSince(from).first()
        val totals = salesDao.observeTotalsSince(from).first()
        val low = stockDao.observeLow().first()
        val top = salesDao.topSellers(from)
        return buildString {
            appendLine("Sales today: ${sales.size}, total ${sales.sumOf { it.totalCents }.rands()}")
            totals.forEach { appendLine("- ${it.method.label}: ${it.total.rands()}") }
            appendLine("Top sellers today: " + (top.joinToString { "${it.name} x${it.qty}" }.ifBlank { "none yet" }))
            appendLine("Low stock: " + (low.joinToString { "${it.name} (${it.quantity} left)" }.ifBlank { "none" }))
        }
    }

    suspend fun seedIfEmpty() {
        if (stockDao.count() == 0) {
            listOf(
                StockItem(name = "Bread", quantity = 20, lowStockThreshold = 6, costCents = 1500, priceCents = 1800),
                StockItem(name = "Cooking oil 750ml", quantity = 12, lowStockThreshold = 4, costCents = 3500, priceCents = 4200),
                StockItem(name = "Airtime R10", quantity = 50, lowStockThreshold = 10, costCents = 950, priceCents = 1000),
                StockItem(name = "Till rolls 57mm", quantity = 8, lowStockThreshold = 5, costCents = 900, priceCents = 1400),
                StockItem(name = "Speedpoint rolls", quantity = 3, lowStockThreshold = 5, costCents = 1200, priceCents = 1800)
            ).forEach { stockDao.upsert(it) }
        }

        if (knowledgeDao.count() == 0) {
            listOf(
                KnowledgeDoc(
                    title = "Store Return & Exchange Policy",
                    category = "Policy",
                    content = "Refunds and exchanges are allowed within 7 days with a valid receipt. Perishable items (milk, bread) cannot be returned once opened."
                ),
                KnowledgeDoc(
                    title = "Wholesale Supplier Contacts",
                    category = "Suppliers",
                    content = "Bread: Albany Driver Sipho 082 555 0199. Drinks: Coca-Cola Depot Sandton 011 400 2000. Airtime: Flash Merchant Support 083 900 0000."
                ),
                KnowledgeDoc(
                    title = "End of Day Cashup Procedure",
                    category = "Operations",
                    content = "Count physical cash in till drawer. Deduct float of R500. Match cash total against Isibalo Express Expected Cash total. Report any variance above R50."
                )
            ).forEach { knowledgeDao.upsert(it) }
        }
    }
}
