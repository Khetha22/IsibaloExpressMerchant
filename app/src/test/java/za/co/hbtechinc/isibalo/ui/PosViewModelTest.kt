package za.co.hbtechinc.isibalo.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import za.co.hbtechinc.isibalo.data.CartLine
import za.co.hbtechinc.isibalo.data.PaymentMethod
import za.co.hbtechinc.isibalo.data.Repository
import za.co.hbtechinc.isibalo.data.StockItem
import za.co.hbtechinc.isibalo.ui.pos.PosViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repo: Repository = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        whenever(repo.stock()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test adding stock item to cart updates total`() = runTest {
        val sampleItem = StockItem(id = 1, name = "Bread", quantity = 10, priceCents = 1800)
        whenever(repo.stock()).thenReturn(flowOf(listOf(sampleItem)))

        val vm = PosViewModel(repo)
        vm.add(sampleItem)

        assertEquals(1, vm.cart.value.size)
        assertEquals("Bread", vm.cart.value.first().name)
        assertEquals(1800L, vm.cart.value.first().unitPriceCents)
    }

    @Test
    fun `test setting quick amount updates checkout total`() = runTest {
        val vm = PosViewModel(repo)
        vm.setQuick("50.00")

        assertEquals("50.00", vm.quickAmount.value)
    }

    @Test
    fun `test charging cart invokes repository checkout`() = runTest {
        val sampleItem = StockItem(id = 1, name = "Milk", quantity = 5, priceCents = 2000)
        whenever(repo.stock()).thenReturn(flowOf(listOf(sampleItem)))

        val vm = PosViewModel(repo)
        vm.add(sampleItem)
        vm.method.value = PaymentMethod.CASH

        val expectedCart = listOf(CartLine(1, "Milk", 2000, 1, 5))

        vm.charge()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repo).checkout(eq(expectedCart), eq(PaymentMethod.CASH))
    }
}
