package za.co.hbtechinc.isibalo.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Card (Reader)"),
    QR_CODE("Scan to Pay (QR)"),
    MOBILE("Mobile Money"),
    GATEWAY("Digital Link")
}

@Entity(tableName = "stock_items")
data class StockItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Int,
    val lowStockThreshold: Int = 5,
    val costCents: Long = 0,
    val priceCents: Long,
    val expiryDate: Long = 0L, // 0 = no expiry set
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    val isLow: Boolean get() = quantity <= lowStockThreshold
    val isExpired: Boolean get() = expiryDate > 0L && expiryDate < System.currentTimeMillis()
    val isExpiringSoon: Boolean get() = expiryDate > 0L && !isExpired && (expiryDate - System.currentTimeMillis() <= 7L * 24 * 60 * 60 * 1000)
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

/** End-of-day reconciliation record. */
@Entity(tableName = "cash_up_records")
data class CashUpRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long = System.currentTimeMillis(),
    val floatCents: Long,
    val expectedCashCents: Long,
    val actualCashCents: Long,
    val varianceCents: Long,
    val cardTotalCents: Long = 0,
    val mobileTotalCents: Long = 0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

/** Knowledge document used for On-Device RAG (Retrieval-Augmented Generation). */
@Entity(tableName = "knowledge_docs")
data class KnowledgeDoc(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)

/** Customer credit / tab book (Isikwelete). */
@Entity(tableName = "customer_debts")
data class CustomerDebt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val phone: String,
    val amountCents: Long,
    val note: String = "",
    val settled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

/** Business expense tracking. */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

/** Petty cash issuance and reconciliation. */
@Entity(tableName = "petty_cash")
data class PettyCash(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipient: String,
    val amountCents: Long,
    val note: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val settled: Boolean = false,
    val synced: Boolean = false
)

/** Merchant Cash Advance (MCA) / Working Capital Loan. */
@Entity(tableName = "merchant_loans")
data class MerchantLoan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val principalCents: Long,
    val feeCents: Long,
    val totalRepayCents: Long,
    val remainingCents: Long,
    val status: String = "Active", // Active, Repaid
    val createdAt: Long = System.currentTimeMillis()
)

/** A line in the POS cart. stockItemId == null means a quick/custom amount. */
data class CartLine(
    val stockItemId: Long?,
    val name: String,
    val unitPriceCents: Long,
    val qty: Int,
    val maxQty: Int = Int.MAX_VALUE
)
