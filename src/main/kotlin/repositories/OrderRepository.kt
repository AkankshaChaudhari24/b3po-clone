package repositories

import models.Order
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


    }
}