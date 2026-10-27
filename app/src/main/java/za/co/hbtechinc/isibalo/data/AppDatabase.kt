package za.co.hbtechinc.isibalo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StockItem::class, Sale::class, SaleItem::class, CashUpRecord::class, KnowledgeDoc::class, CustomerDebt::class, Expense::class, PettyCash::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun salesDao(): SalesDao
    abstract fun cashUpDao(): CashUpDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun customerDebtDao(): CustomerDebtDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun pettyCashDao(): PettyCashDao
}
