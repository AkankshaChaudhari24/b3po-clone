package models

data class CreateOrderInput(val quantity: Int? = null,
                            val esopType: String?,
                            val orderType: String? = null,
                            val price: Int? = null)