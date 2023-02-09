package models

data class OrderRequest(
    var quantity: Long,
    var type: String,
    var price: Long,
    var esopType: String? = "NON-PERFORMANCE"
)
