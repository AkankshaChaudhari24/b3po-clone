package services
import models.Order

class BuyOrderingComparator {
    companion object : Comparator<Order> {
        override fun compare(o1: Order, o2: Order): Int {
            if (o1.orderPrice != o2.orderPrice) {
                if (o1.orderPrice < o2.orderPrice) {
                    return 1
                } else {
                    return -1
                }
            } else {
                if (o1.orderId > o2.orderId) {
                    return 1
                } else {
                    return -1
                }
            }
        }
    }
}