package za.co.hbtechinc.isibalo.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class PaymentMethod(val label: String) { CASH("Cash"), CARD("Card"), MOBILE("Mobile") }

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Int,
    val lowStockThreshold: Int = 5,
    val costCents: Long = 0,
    val priceCents: Long,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    val isLow: Boolean get() = quantity <= lowStockThreshold
}

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalCents: Long,
    val method: PaymentMethod,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [ForeignKey(
        entity = Sale::class, parentColumns = ["id"],
        childColumns = ["saleId"], onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val stockItemId: Long?,
    val name: String,
    val qty: Int,
    val unitPriceCents: Long
)

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(parentColumn = "id", entityColumn = "saleId") val items: List<SaleItem>
)

data class MethodTotal(val method: PaymentMethod, val total: Long)
data class TopSeller(val name: String, val qty: Int, val revenue: Long)

/** A line in the POS cart. stockItemId == null means a quick/custom amount. */
data class CartLine(
    val stockItemId: Long?,
    val name: String,
    val unitPriceCents: Long,
    val qty: Int,
    val maxQty: Int = Int.MAX_VALUE
)
