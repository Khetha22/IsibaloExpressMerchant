package za.co.hbtechinc.isibalo.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StockItemTest {

    @Test
    fun `test low stock threshold check`() {
        val lowItem = StockItem(name = "Bread", quantity = 3, lowStockThreshold = 5, priceCents = 1800)
        val normalItem = StockItem(name = "Milk", quantity = 10, lowStockThreshold = 5, priceCents = 2000)

        assertTrue(lowItem.isLow)
        assertFalse(normalItem.isLow)
    }

    @Test
    fun `test expired and expiring soon checks`() {
        val now = System.currentTimeMillis()
        val expiredItem = StockItem(name = "Milk", quantity = 5, priceCents = 1800, expiryDate = now - 100000L)
        val expiringSoonItem = StockItem(name = "Yogurt", quantity = 5, priceCents = 1500, expiryDate = now + (2L * 24 * 60 * 60 * 1000))
        val freshItem = StockItem(name = "Canned Beans", quantity = 5, priceCents = 1200, expiryDate = now + (30L * 24 * 60 * 60 * 1000))

        assertTrue(expiredItem.isExpired)
        assertTrue(expiringSoonItem.isExpiringSoon)
        assertFalse(freshItem.isExpired)
        assertFalse(freshItem.isExpiringSoon)
    }
}
