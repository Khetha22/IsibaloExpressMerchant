package za.co.hbtechinc.isibalo.desktop.data

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CARD("Card (Reader)"),
    QR_CODE("Scan to Pay (QR)"),
    MOBILE("Mobile Money"),
    GATEWAY("Digital Link")
}

data class StockItem(
    val id: Long = 0,
    val name: String,
    val quantity: Int,
    val lowStockThreshold: Int = 5,
    val costCents: Long = 0,
    val priceCents: Long,
    val expiryDate: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
) {
    val isLow: Boolean get() = quantity <= lowStockThreshold
    val isExpired: Boolean get() = expiryDate > 0L && expiryDate < System.currentTimeMillis()
    val isExpiringSoon: Boolean get() = expiryDate > 0L && !isExpired && (expiryDate - System.currentTimeMillis() <= 7L * 24 * 60 * 60 * 1000)
}

data class Sale(
    val id: Long = 0,
    val totalCents: Long,
    val method: PaymentMethod,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class SaleItem(
    val id: Long = 0,
    val saleId: Long,
    val stockItemId: Long?,
    val name: String,
    val qty: Int,
    val unitPriceCents: Long
)

data class SaleWithItems(
    val sale: Sale,
    val items: List<SaleItem>
)

data class CashUpRecord(
    val id: Long = 0,
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

data class KnowledgeDoc(
    val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)

data class CustomerDebt(
    val id: Long = 0,
    val customerName: String,
    val phone: String,
    val amountCents: Long,
    val note: String = "",
    val settled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class Expense(
    val id: Long = 0,
    val title: String,
    val amountCents: Long,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class PettyCash(
    val id: Long = 0,
    val recipient: String,
    val amountCents: Long,
    val note: String = "",
    val issuedAt: Long = System.currentTimeMillis(),
    val settled: Boolean = false,
    val synced: Boolean = false
)

data class MerchantLoan(
    val id: Long = 0,
    val principalCents: Long,
    val feeCents: Long,
    val totalRepayCents: Long,
    val remainingCents: Long,
    val status: String = "Active",
    val createdAt: Long = System.currentTimeMillis()
)

data class Staff(
    val id: Long = 0,
    val name: String,
    val role: String = "Cashier",
    val dailyWageCents: Long,
    val phone: String = "",
    val active: Boolean = true,
    val synced: Boolean = false
)

data class WhatsAppShareLog(
    val id: Long = 0,
    val title: String,
    val summary: String,
    val sharedAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)

data class CartLine(
    val stockItemId: Long?,
    val name: String,
    val unitPriceCents: Long,
    val qty: Int,
    val maxQty: Int = Int.MAX_VALUE,
    val weightKg: Double? = null,
    val kitchenNote: String = ""
)
