package za.co.hbtechinc.isibalo.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [StockItem::class, Sale::class, SaleItem::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun salesDao(): SalesDao
}
