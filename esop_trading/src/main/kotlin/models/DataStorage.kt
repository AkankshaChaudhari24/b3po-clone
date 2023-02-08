package models
import java.math.BigInteger

class DataStorage {
    companion object {
        const val COMMISSION_FEE_PERCENTAGE = 2.0F
        const val MAX_AMOUNT = 10_000_000
        const val MAX_QUANTITY = 10_000_000
        var TOTAL_FEE_COLLECTED: BigInteger = BigInteger.valueOf(0)
    }
}