package models

import exception.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import repositories.OrderRepository
import repositories.UserRepository
import java.lang.Long.min
import kotlin.math.roundToLong

class Order(
    val userName: String,
    val orderId: Long = 0,
    val quantity: Long,
    val price: Long,
    val type: String,
    var status: String = "Unfilled",
    var esopType: String? = "NON-PERFORMANCE"
) {
    val logger: Logger = LoggerFactory.getLogger(Order::class.java)
    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList()
    var remainingOrderQuantity: Long = quantity


    fun findMinOrderQuantity(order: Order): Long {
        return min(order.remainingOrderQuantity, remainingOrderQuantity)
    }


    fun addOrder() {
        if (type == "BUY") {
            addBuyOrder()
        } else if (type == "SELL") {
            addSellOrder()
        }
    }

    fun addOrderExecutionLogs(orderExecuted: OrderExecutionLogs) {
        if (orderExecuted.orderExecutionQuantity == this.remainingOrderQuantity) {
            this.status = "Filled"
        }
        if (orderExecuted.orderExecutionQuantity < this.remainingOrderQuantity) {
            this.status = "Partially Filled"
        }
        this.remainingOrderQuantity = this.remainingOrderQuantity - orderExecuted.orderExecutionQuantity
        orderExecutionLogs.add(orderExecuted)
    }


    private fun addBuyOrder() {
        val user: User = UserRepository.getUser(userName)!!
        logger.info("User free inventory - {}", user.getFreeInventory())
        logger.info("User free wallet - {}", user.getFreeMoney())
        logger.info("User list size - {}", UserRepository.getUserListSize())
        throwExceptionIfInvalidBuyOrder(quantity, price)
        user.moveFreeMoneyToLockedMoney(quantity * price)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "BUY")
        UserRepository.getUser(userName)?.addOrderHistory(newOrder)
        OrderRepository.addOrderToBuyList(newOrder)
    }

    private fun throwExceptionIfInvalidBuyOrder(orderQuantity: Long, orderPrice: Long) {
        val errorList = ArrayList<String>()
        val transactionAmount = orderQuantity * orderPrice
        val user: User = UserRepository.getUser(userName)!!
        logger.info(
            "user.getFreeInventory() - {} user.getLockedInventory() - {} orderQuantity - {} >=  DataStorage.MAX_QUANTITY - {}",
            user.getFreeInventory(),
            user.getLockedInventory(),
            orderQuantity,
            DataStorage.MAX_QUANTITY
        )
        if (user.getFreeInventory() + user.getLockedInventory() + orderQuantity >= DataStorage.MAX_QUANTITY)
            errorList.add("Inventory threshold will be exceeded")
        if (user.getFreeMoney() < transactionAmount)
            errorList.add("Insufficient balance in wallet")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))
    }

    private fun addSellOrder() {
        if (esopType == "PERFORMANCE")
            addPerformanceSellOrder()
        else if (esopType == "NON-PERFORMANCE")
            addNonPerformanceSellOrder()
    }

    private fun addPerformanceSellOrder() {
        throwExceptionIfInvalidPerformanceEsopSellOrder()
        val user: User = UserRepository.getUser(userName)!!
        user.moveFreePerformanceInventoryToLockedPerformanceInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToPerformanceSellList(newOrder)
    }

    private fun addNonPerformanceSellOrder() {
        throwExceptionIfInvalidNonPerformanceEsopSellOrder()
        val user: User = UserRepository.getUser(userName)!!
        user.moveFreeInventoryToLockedInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToSellList(newOrder)
    }

    private fun throwExceptionIfInvalidNonPerformanceEsopSellOrder() {
        val errorList = ArrayList<String>()
        val user: User = UserRepository.getUser(userName)!!
        val transactionAmount = price * quantity
        val transactionAmountFeeDeducted =
            (transactionAmount * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01)).roundToLong()
        if (user.getFreeInventory() < quantity)
            errorList.add("Insufficient non-performance ESOPs in inventory")
        if (user.getFreeMoney() + user.getLockedMoney() + transactionAmountFeeDeducted > DataStorage.MAX_AMOUNT)
            errorList.add("Wallet threshold will be exceeded")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))
    }

    private fun throwExceptionIfInvalidPerformanceEsopSellOrder() {
        val errorList = ArrayList<String>()
        val transactionAmount = price * quantity
        val user: User = UserRepository.getUser(userName)!!

        if (user.getFreePerformanceInventory() < quantity)
            errorList.add("Insufficient performance ESOPs in inventory")
        if (user.getFreeMoney() + user.getLockedMoney() + transactionAmount > DataStorage.MAX_AMOUNT)
            errorList.add("Wallet threshold will be exceeded")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))
    }

    override fun toString(): String {
        return "username:$userName\norderId:$orderId\norderQuantity:$quantity\norderPrice:$price\norderType:$type\norderStatus:$status\nremainingQuantity:${remainingOrderQuantity}\n"
    }
}