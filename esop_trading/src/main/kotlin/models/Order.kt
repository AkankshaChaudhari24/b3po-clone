package models

import exception.ValidationException
import repositories.OrderRepository
import repositories.UserRepository
import java.lang.Long.min
import kotlin.math.roundToLong

class Order(
    private val userName: String,
    val orderId: Long = 0,
    val quantity: Long,
    val price: Long,
    val type: String,
    var status: String = "Unfilled",
    var esopType: String? = "NON-PERFORMANCE"
) {

    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList()
    var remainingOrderQuantity: Long = quantity
    val user: User = UserRepository.getUser(userName)!!

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

    private fun addBuyOrder() {
        throwExceptionIfInvalidBuyOrder(quantity, price)
        user.moveFreeMoneyToLockedMoney(quantity * price)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "BUY")
        UserRepository.getUser(userName)?.addOrderHistory(newOrder)
        OrderRepository.addOrderToBuyList(newOrder)
    }

    private fun throwExceptionIfInvalidBuyOrder(orderQuantity: Long, orderPrice: Long) {
        val errorList = ArrayList<String>()
        val transactionAmount = orderQuantity * orderPrice

        if (user.getFreeInventory() + user.getLockedInventory() + orderQuantity > DataStorage.MAX_QUANTITY)
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
        user.moveFreePerformanceInventoryToLockedPerformanceInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToPerformanceSellList(newOrder)
    }

    private fun addNonPerformanceSellOrder() {
        throwExceptionIfInvalidNonPerformanceEsopSellOrder()
        user.moveFreeInventoryToLockedInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToSellList(newOrder)
    }

    private fun throwExceptionIfInvalidNonPerformanceEsopSellOrder() {
        val errorList = ArrayList<String>()
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