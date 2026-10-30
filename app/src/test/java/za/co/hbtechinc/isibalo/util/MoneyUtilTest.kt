package za.co.hbtechinc.isibalo.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyUtilTest {

    @Test
    fun `test rands formatting`() {
        val cents = 1850L
        assertEquals("R18.50", cents.rands())
    }

    @Test
    fun `test string to cents conversion`() {
        assertEquals(1850L, "18.50".toCents())
        assertEquals(1000L, "10".toCents())
        assertEquals(null, "invalid".toCents())
    }

    @Test
    fun `test 2 point 5 percent merchant commission calculation`() {
        val totalCents = 10000L // R100.00
        val commission = totalCents.calculateCommissionCents()
        assertEquals(250L, commission) // R2.50 commission
    }
}
