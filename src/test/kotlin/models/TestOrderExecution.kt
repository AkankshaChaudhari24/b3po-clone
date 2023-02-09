package models

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.OrderRepository
import repositories.UserRepository
import services.OrderServices
import services.saveUser
import kotlin.math.roundToLong

class TestOrderExecution {
    private lateinit var buyOrderOne: Order
    private lateinit var buyOrderTwo: Order
    private lateinit var buyOrderThree: Order
    private lateinit var buyOrderFour: Order
    private lateinit var sellOrderOne: Order
    private lateinit var sellOrderTwo: Order
    private lateinit var sellOrderThree: Order
    private lateinit var performanceOrderOne: Order
    private lateinit var performanceOrderTwo: Order
    private lateinit var performanceOrderThree: Order

    @BeforeEach
    fun setup() {
        val buyer = User("jake", "Jake", "Peralta", "9844427549", "jake@gmail.com") //Buyer
        buyer.addMoneyToWallet(10000)
        val seller = User("amy", "Amy", "Santiago", "9472919384", "amy@gmail.com") //Seller
        seller.addEsopToInventory(100, "NON-PERFORMANCE")
        seller.addEsopToInventory(100, "PERFORMANCE")

        buyOrderOne = Order(userName = buyer.username, quantity = 5, price = 10, type = "BUY")
        buyOrderTwo = Order(userName = buyer.username, quantity = 1, price = 10, type = "BUY")
        buyOrderThree = Order(userName = buyer.username, quantity = 15, price = 10, type = "BUY")
        buyOrderFour = Order(userName = buyer.username, quantity = 1, price = 5, type = "BUY")
        sellOrderOne = Order(userName = seller.username, quantity = 15, price = 10, type = "SELL")
        sellOrderTwo = Order(userName = seller.username, quantity = 1, price = 5, type = "SELL")
        sellOrderThree = Order(userName = seller.username, quantity = 1, price = 10, type = "SELL")
        performanceOrderOne =
            Order(userName = seller.username, quantity = 1, price = 100, type = "SELL", esopType = "PERFORMANCE")
        performanceOrderTwo =
            Order(userName = seller.username, quantity = 1, price = 10, type = "SELL", esopType = "PERFORMANCE")
        performanceOrderThree =
            Order(userName = seller.username, quantity = 1, price = 5, type = "SELL", esopType = "PERFORMANCE")

        saveUser(buyer)
        saveUser(seller)
    }

    @AfterEach
    fun tearDown() {
        UserRepository.clearUserList()
        UserRepository.clearEmailList()
        UserRepository.clearPhoneNumberList()


        OrderRepository.clearBuyList()
        OrderRepository.clearSellList()
        OrderRepository.clearPerformanceSellList()
        OrderRepository.setOrderId(1L)
        OrderRepository.setOrderExecutionId(1L)
    }

    @Test
    fun `multiple buy orders by one user and one sell order by another user to fulfill them completely`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!
        val expectedSellerWallet = (150 * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01)).roundToLong()

        OrderRepository.addOrder(buyOrderOne)
        OrderRepository.addOrder(buyOrderOne)
        OrderRepository.addOrder(buyOrderOne)
        OrderRepository.addOrder(sellOrderOne)
        OrderServices.matchOrders()
        assert(OrderRepository.getBuyList().isEmpty())
        assert(OrderRepository.getSellList().isEmpty())
        assertEquals(9850, buyer.getFreeMoney())
        assertEquals(15, buyer.getFreeInventory())
        assertEquals(expectedSellerWallet, seller.getFreeMoney())
        assertEquals(85, seller.getFreeInventory())
    }

    @Test
    fun `should take sell price as order price when buy price is higher`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!
        val expectedSellerWallet = (5 * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01)).roundToLong()

        OrderRepository.addOrder(buyOrderTwo)
        OrderRepository.addOrder(sellOrderTwo)
        OrderServices.matchOrders()
        assertEquals(10000 - 5, buyer.getFreeMoney())
        assertEquals(expectedSellerWallet, seller.getFreeMoney())
    }

    @Test
    fun `should prioritize sell order that has lower price`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!

        OrderRepository.addOrder(sellOrderThree)
        OrderRepository.addOrder(sellOrderTwo)
        OrderRepository.addOrder(buyOrderTwo)
        OrderServices.matchOrders()
        assertEquals("Unfilled", seller.orders[0].status)
        assertEquals(10, seller.orders[0].price)
        assertEquals("Filled", seller.orders[1].status)
        assertEquals(5, seller.orders[1].price)
        assertEquals("Filled", buyer.orders[0].status)
        assertEquals(10, buyer.orders[0].price)
        assertEquals(10000 - 5, buyer.getFreeMoney())
    }

    @Test
    fun `should prioritize buy order that has higher price`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!

        OrderRepository.addOrder(buyOrderFour)
        OrderRepository.addOrder(buyOrderTwo)
        OrderRepository.addOrder(sellOrderTwo)
        OrderServices.matchOrders()
        assertEquals("Unfilled", buyer.orders[0].status)
        assertEquals(5, buyer.orders[0].price)
        assertEquals("Filled", buyer.orders[1].status)
        assertEquals(10, buyer.orders[1].price)
        assertEquals("Filled", seller.orders[0].status)
        assertEquals(5, seller.orders[0].price)
    }

    @Test
    fun `should prioritize performance ESOP sell orders over non-performance ESOP sell orders`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!

        OrderRepository.addOrder(sellOrderTwo)
        OrderRepository.addOrder(performanceOrderTwo)
        OrderRepository.addOrder(buyOrderTwo)
        OrderServices.matchOrders()
        assertEquals("Unfilled", seller.orders[0].status)
        assertEquals(5, seller.orders[0].price)
        assertEquals("Filled", seller.orders[1].status)
        assertEquals(10, seller.orders[1].price)
        assertEquals("Filled", buyer.orders[0].status)
        assertEquals(10, buyer.orders[0].price)
        assertEquals(10000 - 10, buyer.getFreeMoney())
    }

    @Test
    fun `buyer should get non-performance ESOP even if seller sells performance ESOPs`() {
        val buyer = UserRepository.getUser("jake")!!

        OrderRepository.addOrder(performanceOrderTwo)
        OrderRepository.addOrder(buyOrderTwo)
        OrderServices.matchOrders()
        assertEquals(0, buyer.getLockedPerformanceInventory())
        assertEquals(0, buyer.getFreePerformanceInventory())
        assertEquals(0, buyer.getLockedInventory())
        assertEquals(1, buyer.getFreeInventory())
    }

    @Test
    fun `should prioritize order that came first among multiple performance ESOP sell orders irrespective of price`() {
        val buyer = UserRepository.getUser("jake")!!
        val seller = UserRepository.getUser("amy")!!

        OrderRepository.addOrder(performanceOrderTwo)
        OrderRepository.addOrder(performanceOrderThree)
        OrderRepository.addOrder(buyOrderTwo)
        OrderServices.matchOrders()
        assertEquals(10000 - 10, buyer.getFreeMoney())
        assertEquals(0, buyer.getLockedMoney())
        assertEquals("Filled", seller.orders[0].status)
        assertEquals(10, seller.orders[0].price)
        assertEquals("Unfilled", seller.orders[1].status)
        assertEquals(5, seller.orders[1].price)
        assertEquals("Filled", buyer.orders[0].status)
        assertEquals(10, buyer.orders[0].price)
    }
}