package models

import exception.OrderExceptions
import repositories.OrderRepository
import repositories.UserRepository

class Order(
    val userName: String,
    val orderId: Long = 0,
    val quantity: Long,
    val price: Long,
    val type: String,
    var status: String = "Unfilled",
    var esopType: String? = "NON-PERFORMANCE"
) {
    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList()
    var remainingOrderQuantity: Long = quantity
    private val orderExceptions: OrderExceptions = OrderExceptions()


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
        orderExceptions.throwExceptionIfInvalidBuyOrder(this)
        user.moveFreeMoneyToLockedMoney(quantity * price)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "BUY")
        UserRepository.getUser(userName)?.addOrderHistory(newOrder)
        OrderRepository.addOrderToBuyList(newOrder)
    }


    private fun addSellOrder() {
        if (esopType == "PERFORMANCE")
            addPerformanceSellOrder()
        else if (esopType == "NON-PERFORMANCE")
            addNonPerformanceSellOrder()
    }

    private fun addPerformanceSellOrder() {
        orderExceptions.throwExceptionIfInvalidPerformanceEsopSellOrder(this)
        val user: User = UserRepository.getUser(userName)!!
        user.moveFreePerformanceInventoryToLockedPerformanceInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToPerformanceSellList(newOrder)
    }

    private fun addNonPerformanceSellOrder() {
        orderExceptions.throwExceptionIfInvalidNonPerformanceEsopSellOrder(this)
        val user: User = UserRepository.getUser(userName)!!
        user.moveFreeInventoryToLockedInventory(quantity)
        val newOrder = Order(userName, OrderRepository.generateOrderId(), quantity, price, "SELL")
        user.addOrderHistory(newOrder)
        OrderRepository.addOrderToSellList(newOrder)
    }


    override fun toString(): String {
        return "username:$userName\norderId:$orderId\norderQuantity:$quantity\norderPrice:$price\norderType:$type\norderStatus:$status\nremainingQuantity:${remainingOrderQuantity}\n"
    }
}