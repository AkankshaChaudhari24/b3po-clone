package models

class Order(
    val userName: String,
    val orderId: Long,
    val quantity: Long,
    val price: Long,
    val type: String,
    var status: String = "Unfilled"
) {
    val orderExecutionLogs: ArrayList<OrderExecutionLogs> = ArrayList()
    var remainingOrderQuantity: Long = quantity

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

    override fun toString(): String {
        return "username:$userName\norderId:$orderId\norderQuantity:$quantity\norderPrice:$price\norderType:$type\norderStatus:$status\nremainingQuantity:${remainingOrderQuantity}\n"
    }
}