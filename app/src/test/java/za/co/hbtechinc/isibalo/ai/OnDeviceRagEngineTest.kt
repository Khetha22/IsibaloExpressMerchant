package za.co.hbtechinc.isibalo.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import za.co.hbtechinc.isibalo.data.KnowledgeDoc

class OnDeviceRagEngineTest {

    @Test
    fun `test vector search retrieves relevant store policy document`() {
        val docs = listOf(
            KnowledgeDoc(id = 1, title = "Return Policy", content = "Refunds allowed within 7 days with valid receipt.", category = "Policy"),
            KnowledgeDoc(id = 2, title = "Supplier Contact", content = "Albany Bread Driver Sipho 082 555 0199", category = "Suppliers"),
            KnowledgeDoc(id = 3, title = "Cashup Rules", content = "Count physical cash in till drawer.", category = "Operations")
        )

        val results = OnDeviceRagEngine.search("refund receipt policy", docs, topK = 1)

        assertEquals(1, results.size)
        assertEquals("Return Policy", results.first().doc.title)
        assertTrue(results.first().score > 0.1)
    }

    @Test
    fun `test vector search returns empty list for blank query`() {
        val docs = listOf(
            KnowledgeDoc(id = 1, title = "Return Policy", content = "Refunds allowed within 7 days.", category = "Policy")
        )
        val results = OnDeviceRagEngine.search("", docs, topK = 3)
        assertTrue(results.isEmpty())
    }
}
