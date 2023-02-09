package models

import java.math.BigInteger

data class AddToInventoryInput(val esop_type: String?, val quantity: BigInteger? =null)