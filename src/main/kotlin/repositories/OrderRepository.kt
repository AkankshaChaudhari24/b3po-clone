package repositories

import exception.OrderExceptions
import models.Order
import models.User
import services.BuyOrderingComparator
import services.SellOrderingComparator
import java.util.*

open class OrderRepository {
    companion object {

        private val buyList = PriorityQueue<Order>(BuyOrderingComparator)
        private val sellList = PriorityQueue<Order>(SellOrderingComparator)
        private val performanceSellList = LinkedList<Order>()

        private var orderId: Long = 1L
        private var orderExecutionId = 1L

        fun getBuyList() = buyList
        fun getSellList() = sellList
        fun getPerformanceSellList() = performanceSellList
        fun clearBuyList() = buyList.clear()
        fun clearSellList() = sellList.clear()
        fun clearPerformanceSellList() = performanceSellList.clear()
        fun setOrderId(orderId: Long) {
            this.orderId = orderId
        }

        fun setOrderExecutionId(orderExecutionId: Long) {
            this.orderExecutionId = orderExecutionId
        }

        @Synchronized
        fun addOrderToBuyList(order: Order) {
            buyList.add(order)
        }

        @Synchronized
        fun addOrderToSellList(order: Order) {
            sellList.add(order)
        }

        @Synchronized
        fun generateOrderId(): Long {
            return orderId++
        }

        @Synchronized
        fun generateOrderExecutionId(): Long {
            return orderExecutionId++
        }

        @Synchronized
        fun addOrderToPerformanceSellList(order: Order) {
            performanceSellList.add(order)
        }

        fun addOrder(order: Order) {
            if (order.type == "BUY") {
                addBuyOrder(order)
            } else if (order.type == "SELL") {
                addSellOrder(order)
            }
        }


        private fun addBuyOrder(order: Order) {
            val user: User = UserRepository.getUser(order.userName)!!
            OrderExceptions().throwExceptionIfInvalidBuyOrder(order)
            user.moveFreeMoneyToLockedMoney(order.quantity * order.price)
            val newOrder = Order(order.userName, generateOrderId(), order.quantity, order.price, "BUY")
            UserRepository.getUser(order.userName)?.addOrderHistory(newOrder)
            addOrderToBuyList(newOrder)
        }


        private fun addSellOrder(order: Order) {
            if (order.esopType == "PERFORMANCE") addPerformanceSellOrder(order)
            else if (order.esopType == "NON-PERFORMANCE") addNonPerformanceSellOrder(order)
        }

        private fun addPerformanceSellOrder(order: Order) {
            OrderExceptions().throwExceptionIfInvalidPerformanceEsopSellOrder(order)
            val user: User = UserRepository.getUser(order.userName)!!
            user.moveFreePerformanceInventoryToLockedPerformanceInventory(order.quantity)
            val newOrder = Order(order.userName, generateOrderId(), order.quantity, order.price, "SELL")
            user.addOrderHistory(newOrder)
            addOrderToPerformanceSellList(newOrder)
        }

        private fun addNonPerformanceSellOrder(order: Order) {
            OrderExceptions().throwExceptionIfInvalidNonPerformanceEsopSellOrder(order)
            val user: User = UserRepository.getUser(order.userName)!!
            user.moveFreeInventoryToLockedInventory(order.quantity)
            val newOrder = Order(order.userName, generateOrderId(), order.quantity, order.price, "SELL")
            user.addOrderHistory(newOrder)
            addOrderToSellList(newOrder)
        }

    }
}