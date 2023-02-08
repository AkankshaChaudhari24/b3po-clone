package models

import exception.ValidationException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repositories.OrderRepository
import repositories.UserRepository
import services.saveUser

class TestOrderCreation {
    private lateinit var buyOrderOne: Order
    private lateinit var buyOrderTwo: Order
    private lateinit var buyOrderThree: Order
    private lateinit var sellOrderOne: Order
    private lateinit var sellOrderTwo: Order
    private lateinit var user: User
    private lateinit var performanceOrderOne: Order
    private lateinit var performanceOrderTwo: Order

    @BeforeEach
    fun `set up`() {
        user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )
        saveUser(user)
        buyOrderOne = Order(userName = user.username, quantity = 1, price = 100, type = "BUY")
        buyOrderTwo = Order(userName = user.username, quantity = 2, price = 100, type = "BUY")
        buyOrderThree = Order(userName = user.username, quantity = 15, price = 10, type = "BUY")
        sellOrderOne = Order(userName = user.username, quantity = 1, price = 100, type = "SELL")
        sellOrderTwo = Order(userName = user.username, quantity = 1, price = 15, type = "SELL")
        performanceOrderOne =
            Order(userName = user.username, quantity = 1, price = 100, type = "SELL", esopType = "PERFORMANCE")
        performanceOrderTwo =
            Order(userName = user.username, quantity = 1, price = 10, type = "SELL", esopType = "PERFORMANCE")
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
    fun `can create buy order if user has money in wallet`() {

        user.addMoneyToWallet(100)

        buyOrderOne.addOrder()

        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create buy order if user doesn't have enough money in wallet`() {


        val exception = assertThrows(ValidationException::class.java) {
            buyOrderOne.addOrder()
        }
        val errors = exception.errorResponse.error

        assertEquals("Insufficient balance in wallet", errors[0])
        assertEquals(0, user.getFreeMoney())
        assertEquals(0, user.getLockedMoney())
    }

    @Test
    fun `creating buy order moves money to locked wallet`() {

        user.addMoneyToWallet(100)

        buyOrderOne.addOrder()

        assertEquals(0, user.getFreeMoney())
        assertEquals(100, user.getLockedMoney())
    }

    @Test
    fun `correct buy order is created`() {

        user.addMoneyToWallet(100)

        buyOrderOne.addOrder()

        assertEquals("Unfilled", OrderRepository.getBuyList().peek().status)
        assertEquals(1, OrderRepository.getBuyList().peek().quantity)
        assertEquals("BUY", OrderRepository.getBuyList().peek().type)
        assertEquals(100, OrderRepository.getBuyList().peek().price)
        assertEquals(0, OrderRepository.getBuyList().peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating buy order adds order to global buy list`() {

        user.addMoneyToWallet(100)

        buyOrderOne.addOrder()

        assertEquals(1, OrderRepository.getBuyList().size)
        assertEquals(user.orders[0], OrderRepository.getBuyList().peek())
    }

    @Test
    fun `can create sell order if user has enough esops`() {
        user.addEsopToInventory(1)

        sellOrderOne.addOrder()
        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create sell order if user doesn't have enough esops in inventory`() {

        val exception = assertThrows(ValidationException::class.java) { sellOrderOne.addOrder() }
        val errors = exception.errorResponse.error

        assertEquals("Insufficient non-performance ESOPs in inventory", errors[0])
        assertEquals(0, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
    }

    @Test
    fun `creating sell order locks esops`() {
        user.addEsopToInventory(1)

        sellOrderOne.addOrder()
        assertEquals(0, user.getFreeInventory())
        assertEquals(1, user.getLockedInventory())
    }

    @Test
    fun `correct sell order is created`() {
        user.addEsopToInventory(1)

        sellOrderOne.addOrder()
        assertEquals("Unfilled", OrderRepository.getSellList().peek().status)
        assertEquals(1, OrderRepository.getSellList().peek().quantity)
        assertEquals("SELL", OrderRepository.getSellList().peek().type)
        assertEquals(100, OrderRepository.getSellList().peek().price)
        assertEquals(0, OrderRepository.getSellList().peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating sell order adds order to global sell list`() {
        user.addEsopToInventory(1)

        sellOrderOne.addOrder()

        assertEquals(1, OrderRepository.getSellList().size)
        assertEquals(user.orders[0], OrderRepository.getSellList().peek())
    }

    @Test
    fun `can create performance sell order if user has enough performance esops`() {
        user.addEsopToInventory(1, "PERFORMANCE")

        performanceOrderOne.addOrder()
        assertEquals(1, user.orders.size)
    }

    @Test
    fun `cannot create performance sell order if user doesn't have enough performance esops in inventory`() {

        val exception = assertThrows(ValidationException::class.java) {
            performanceOrderOne.addOrder()
        }
        val errors = exception.errorResponse.error

        assertEquals("Insufficient performance ESOPs in inventory", errors[0])
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
    }

    @Test
    fun `creating performance sell order locks esops`() {
        user.addEsopToInventory(1, "PERFORMANCE")

        performanceOrderOne.addOrder()
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(1, user.getLockedPerformanceInventory())
    }

    @Test
    fun `correct performance sell order is created`() {
        user.addEsopToInventory(1, "PERFORMANCE")

        performanceOrderOne.addOrder()
        assertEquals("Unfilled", OrderRepository.getPerformanceSellList().peek().status)
        assertEquals(1, OrderRepository.getPerformanceSellList().peek().quantity)
        assertEquals("SELL", OrderRepository.getPerformanceSellList().peek().type)
        assertEquals(100, OrderRepository.getPerformanceSellList().peek().price)
        assertEquals(0, OrderRepository.getPerformanceSellList().peek().orderExecutionLogs.size)
    }

    @Test
    fun `creating performance sell order adds order to global performance sell list`() {
        user.addEsopToInventory(1, "PERFORMANCE")

        performanceOrderOne.addOrder()
        assertEquals(1, OrderRepository.getPerformanceSellList().size)
        assertEquals(user.orders[0], OrderRepository.getPerformanceSellList().peek())
    }

    @Test
    fun `order details is initially empty`() {
        val orderDetails = user.getOrderDetails()

        assert(orderDetails.keys.contains("order_history"))
        assertEquals(0, orderDetails["order_history"]!!.size)
    }

    @Test
    fun `order details for unfilled order is set correctly`() {
        user.addMoneyToWallet(100)
        buyOrderOne.addOrder()
        val orderDetails = user.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        assertEquals(
            "{order_id=1, quantity=1, type=BUY, price=100, unfilled=[{price=100, quantity=1}]}",
            orderDetails["order_history"]!![0].toString()
        )
    }

    @Test
    fun `order details for partially filled order is set correctly`() {
        val buyer = User(
            firstName = "user1",
            lastName = "user1",
            emailId = "user1@example.com",
            phoneNumber = "+911234567891",
            username = "user1"
        )
        val seller = User(
            firstName = "user2",
            lastName = "user2",
            emailId = "user2@example.com",
            phoneNumber = "+911234567892",
            username = "user2"
        )
        saveUser(buyer)
        saveUser(seller)
        buyer.addMoneyToWallet(200)
        seller.addEsopToInventory(1)
        buyOrderTwo.addOrder()
        sellOrderOne.addOrder()
        val orderDetails = buyer.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        assertEquals(
            "{order_id=1, quantity=2, type=BUY, price=100, partially_filled=[{price=100, quantity=1}], unfilled=[{price=100, quantity=1}]}",
            orderDetails["order_history"]!![0].toString()
        )
    }

    @Test
    fun `order details for fully filled order is set correctly`() {
        val buyer = User(
            firstName = "user1",
            lastName = "user1",
            emailId = "user1@example.com",
            phoneNumber = "+911234567891",
            username = "user1"
        )
        val seller = User(
            firstName = "user2",
            lastName = "user2",
            emailId = "user2@example.com",
            phoneNumber = "+911234567892",
            username = "user2"
        )
        saveUser(buyer)
        saveUser(seller)

        buyer.addMoneyToWallet(200)
        seller.addEsopToInventory(1)

        buyOrderTwo = Order(userName = "user1", quantity = 2, price = 100, type = "BUY")
        val sellOrderOne = Order(userName = "user2", quantity = 1, price = 15, type = "SELL")

        buyOrderTwo.addOrder()
        sellOrderOne.addOrder()

        val orderDetails = seller.getOrderDetails()

        assertEquals(1, orderDetails.size)
        assert(orderDetails.keys.contains("order_history"))
        assertEquals(
            "{order_id=2, quantity=2, type=SELL, price=15, filled=[{price=15, quantity=1}]}",
            orderDetails["order_history"]!![0].toString()
        )
    }

    @Test
    fun `can log or print an order`() {
        user.addEsopToInventory(1, "PERFORMANCE")
        val expected = """
            username:user
            orderId:1
            orderQuantity:1
            orderPrice:10
            orderType:SELL
            orderStatus:Unfilled
            remainingQuantity:1

        """.trimIndent()
        performanceOrderTwo.addOrder()
        assertEquals(expected, user.orders[0].toString())
    }

    @Test
    fun `cannot create sell order that will cause wallet limit to be exceeded`() {
        user.addMoneyToWallet(DataStorage.MAX_AMOUNT - 10L)
        user.addEsopToInventory(1)

        val exception = assertThrows(ValidationException::class.java) { sellOrderTwo.addOrder() }
        val errors = exception.errorResponse.error

        assertEquals(1, errors.size)
        assertEquals("Wallet threshold will be exceeded", errors[0])
    }

    @Test
    fun `cannot create buy order that will cause inventory limit to be exceeded`() {
        val logger: Logger = LoggerFactory.getLogger(TestOrderCreation::class.java)
        user.addEsopToInventory(DataStorage.MAX_QUANTITY - 10L)
        user.addMoneyToWallet(150)
        logger.info("User free wallet - {}. User inventory free - {}", user.getFreeMoney(), user.getFreeInventory())
        logger.info("User list size - {}", UserRepository.getUserListSize())

        val exception = assertThrows(ValidationException::class.java) { buyOrderThree.addOrder() }
        val errors = exception.errorResponse.error

        assertEquals(1, errors.size)
        assertEquals("Inventory threshold will be exceeded", errors[0])
    }
}