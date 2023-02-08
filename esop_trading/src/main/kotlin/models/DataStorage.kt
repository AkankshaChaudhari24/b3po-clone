package models
import services.BuyOrderingComparator
import services.SellOrderingComparator
import java.math.BigInteger
import java.util.*

class DataStorage {
    companion object {
        val userList: HashMap<String, User> = HashMap()
        val registeredEmails = mutableSetOf<String>()
        val registeredPhoneNumbers = mutableSetOf<String>()

        var orderId: Long = 1L
        var orderExecutionId = 1L

        val buyList = PriorityQueue<Order>(BuyOrderingComparator)
        val sellList = PriorityQueue<Order>(SellOrderingComparator)
        val performanceSellList = LinkedList<Order>()

        const val COMMISSION_FEE_PERCENTAGE = 2.0F
        const val MAX_AMOUNT = 10_000_000
        const val MAX_QUANTITY = 10_000_000
        var TOTAL_FEE_COLLECTED: BigInteger = BigInteger.valueOf(0)
    }
}