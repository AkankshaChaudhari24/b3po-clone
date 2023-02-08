package services

import models.Order

class SellOrderingComparator {
    companion object : Comparator<Order> {
        override fun compare(o1: Order, o2: Order): Int {
            return if (o1.price != o2.price) {
                if (o1.price > o2.price) {
                    1
                } else {
                    -1
                }
            } else {
                if (o1.orderId > o2.orderId) {
                    1
                } else {
                    -1
                }
            }
        }
    }
}