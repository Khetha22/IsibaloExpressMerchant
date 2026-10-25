package za.co.hbtechinc.isibalo.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Query("SELECT * FROM stock_items ORDER BY name")
    fun observeAll(): Flow<List<StockItem>>

    @Query("SELECT * FROM stock_items WHERE quantity <= lowStockThreshold ORDER BY quantity")
    fun observeLow(): Flow<List<StockItem>>

    @Upsert suspend fun upsert(item: StockItem)
    @Delete suspend fun delete(item: StockItem)

    @Query("UPDATE stock_items SET quantity = MAX(quantity - :qty, 0), synced = 0, updatedAt = :now WHERE id = :id")
    suspend fun decrement(id: Long, qty: Int, now: Long)

    @Query("SELECT COUNT(*) FROM stock_items") suspend fun count(): Int
    @Query("SELECT * FROM stock_items WHERE synced = 0") suspend fun unsynced(): List<StockItem>
    @Query("UPDATE stock_items SET synced = 1 WHERE id IN (:ids)") suspend fun markSynced(ids: List<Long>)
}

@Dao
interface SalesDao {
    @Insert suspend fun insertSale(sale: Sale): Long
    @Insert suspend fun insertItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales WHERE createdAt >= :from ORDER BY createdAt DESC")
    fun observeSince(from: Long): Flow<List<Sale>>

    @Query("SELECT method, SUM(totalCents) AS total FROM sales WHERE createdAt >= :from GROUP BY method")
    fun observeTotalsSince(from: Long): Flow<List<MethodTotal>>

    @Query("SELECT COUNT(*) FROM sales WHERE synced = 0")
    fun observePending(): Flow<Int>

    @Query(
        "SELECT i.name AS name, SUM(i.qty) AS qty, SUM(i.qty * i.unitPriceCents) AS revenue " +
            "FROM sale_items i JOIN sales s ON s.id = i.saleId WHERE s.createdAt >= :from " +
            "GROUP BY i.name ORDER BY qty DESC LIMIT 5"
    )
    suspend fun topSellers(from: Long): List<TopSeller>

    @Transaction @Query("SELECT * FROM sales WHERE synced = 0")
    suspend fun unsynced(): List<SaleWithItems>

    @Query("UPDATE sales SET synced = 1 WHERE id IN (:ids)") suspend fun markSynced(ids: List<Long>)
}
