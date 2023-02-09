package models

data class OrderResponse(
    var quantity: Long,
    var type: String,
    var price: Long
)
