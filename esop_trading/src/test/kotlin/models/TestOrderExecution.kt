package models

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.saveUser
import kotlin.math.roundToLong
class TestOrderExecution {
    @BeforeEach
    fun setup(){
        val buyer = User("jake", "Jake", "Peralta", "9844427549", "jake@gmail.com") //Buyer
        buyer.addMoneyToWallet(10000)
        val seller = User("amy", "Amy", "Santiago", "9472919384", "amy@gmail.com") //Seller
        seller.addEsopToInventory(100, "NON-PERFORMANCE")
        seller.addEsopToInventory(100, "PERFORMANCE")

        saveUser(buyer)
        saveUser(seller)
    }

    @AfterEach
    fun tearDown(){
        DataStorage.userList.clear()
        DataStorage.registeredEmails.clear()
        DataStorage.registeredPhoneNumbers.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.orderId = 1
        DataStorage.orderExecutionId = 1
    }
    @Test
    fun `multiple buy orders by one user and one sell order by another user to fulfill them completely`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!
        val expectedSellerWallet = (150*(1-DataStorage.COMMISSION_FEE_PERCENTAGE*0.01)).roundToLong()

        buyer.addOrderToExecutionQueue(5, "BUY", 10)
        buyer.addOrderToExecutionQueue(5, "BUY", 10)
        buyer.addOrderToExecutionQueue(5, "BUY", 10)
        seller.addOrderToExecutionQueue(15, "SELL", 10)

        assert(DataStorage.buyList.isEmpty())
        assert(DataStorage.sellList.isEmpty())
        assertEquals(9850, buyer.getFreeMoney())
        assertEquals(15, buyer.getFreeInventory())
        assertEquals(expectedSellerWallet, seller.getFreeMoney())
        assertEquals(85, seller.getFreeInventory())
    }

    @Test
    fun `should take sell price as order price when buy price is higher`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!
        val expectedSellerWallet = (5*(1-DataStorage.COMMISSION_FEE_PERCENTAGE*0.01)).roundToLong()

        buyer.addOrderToExecutionQueue(1, "BUY", 10)
        seller.addOrderToExecutionQueue(1, "SELL", 5)

        assertEquals(10000 - 5, buyer.getFreeMoney())
        assertEquals(expectedSellerWallet, seller.getFreeMoney())
    }

    @Test
    fun `should prioritize sell order that has lower price`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!

        seller.addOrderToExecutionQueue(1, "SELL", 10)
        seller.addOrderToExecutionQueue(1,"SELL", 5)
        buyer.addOrderToExecutionQueue(1, "BUY", 10)

        assertEquals("Unfilled", seller.orders[0].orderStatus)
        assertEquals(10, seller.orders[0].orderPrice)
        assertEquals("Filled", seller.orders[1].orderStatus)
        assertEquals(5, seller.orders[1].orderPrice)
        assertEquals("Filled", buyer.orders[0].orderStatus)
        assertEquals(10, buyer.orders[0].orderPrice)
        assertEquals(10000-5, buyer.getFreeMoney())
    }

    @Test
    fun `should prioritize buy order that has higher price`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!

        buyer.addOrderToExecutionQueue(1,"BUY",5)
        buyer.addOrderToExecutionQueue(1,"BUY",10)
        seller.addOrderToExecutionQueue(1, "SELL", 5)

        assertEquals("Unfilled", buyer.orders[0].orderStatus)
        assertEquals(5, buyer.orders[0].orderPrice)
        assertEquals("Filled", buyer.orders[1].orderStatus)
        assertEquals(10, buyer.orders[1].orderPrice)
        assertEquals("Filled", seller.orders[0].orderStatus)
        assertEquals(5, seller.orders[0].orderPrice)
    }

    @Test
    fun `should prioritize performance ESOP sell orders over non-performance ESOP sell orders`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!

        seller.addOrderToExecutionQueue(1, "SELL", 5, "NON-PERFORMANCE")
        seller.addOrderToExecutionQueue(1, "SELL", 10, "PERFORMANCE")
        buyer.addOrderToExecutionQueue(1,"BUY", 10)

        assertEquals("Unfilled", seller.orders[0].orderStatus)
        assertEquals(5, seller.orders[0].orderPrice)
        assertEquals("Filled", seller.orders[1].orderStatus)
        assertEquals(10, seller.orders[1].orderPrice)
        assertEquals("Filled", buyer.orders[0].orderStatus)
        assertEquals(10, buyer.orders[0].orderPrice)
        assertEquals(10000-10, buyer.getFreeMoney())
    }

    @Test
    fun `buyer should get non-performance ESOP even if seller sells performance ESOPs`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!

        seller.addOrderToExecutionQueue(1, "SELL", 10, "PERFORMANCE")
        buyer.addOrderToExecutionQueue(1,"BUY", 10)

        assertEquals(0, buyer.getLockedPerformanceInventory())
        assertEquals(0, buyer.getFreePerformanceInventory())
        assertEquals(0, buyer.getLockedInventory())
        assertEquals(1, buyer.getFreeInventory())
    }

    @Test
    fun `should prioritize order that came first among multiple performance ESOP sell orders irrespective of price`(){
        val buyer = DataStorage.userList["jake"]!!
        val seller = DataStorage.userList["amy"]!!

        seller.addOrderToExecutionQueue(1, "SELL", 10, "PERFORMANCE")
        seller.addOrderToExecutionQueue(1, "SELL", 5, "PERFORMANCE")
        buyer.addOrderToExecutionQueue(1,"BUY",10)

        assertEquals(10000-10, buyer.getFreeMoney())
        assertEquals(0,buyer.getLockedMoney())
        assertEquals("Filled", seller.orders[0].orderStatus)
        assertEquals(10, seller.orders[0].orderPrice)
        assertEquals("Unfilled", seller.orders[1].orderStatus)
        assertEquals(5, seller.orders[1].orderPrice)
        assertEquals("Filled", buyer.orders[0].orderStatus)
        assertEquals(10, buyer.orders[0].orderPrice)
    }
}