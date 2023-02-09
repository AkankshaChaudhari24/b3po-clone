package exception

import models.DataStorage
import models.ErrorResponse
import models.Order
import models.User
import repositories.UserRepository
import kotlin.math.roundToLong

class OrderExceptions() {
    fun throwExceptionIfInvalidBuyOrder(order: Order) {
        val errorList = ArrayList<String>()
        val transactionAmount = order.quantity * order.price
        val user: User = UserRepository.getUser(order.userName)!!
        if (user.getFreeInventory() + user.getLockedInventory() + order.quantity >= DataStorage.MAX_QUANTITY)
            errorList.add("Inventory threshold will be exceeded")
        if (user.getFreeMoney() < transactionAmount)
            errorList.add("Insufficient balance in wallet")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))
    }

    fun throwExceptionIfInvalidNonPerformanceEsopSellOrder(order: Order) {
        val errorList = ArrayList<String>()
        val user: User = UserRepository.getUser(order.userName)!!
        val transactionAmount = order.price * order.quantity
        val transactionAmountFeeDeducted =
            (transactionAmount * (1 - DataStorage.COMMISSION_FEE_PERCENTAGE * 0.01)).roundToLong()
        if (user.getFreeInventory() < order.quantity)
            errorList.add("Insufficient non-performance ESOPs in inventory")
        if (user.getFreeMoney() + user.getLockedMoney() + transactionAmountFeeDeducted > DataStorage.MAX_AMOUNT)
            errorList.add("Wallet threshold will be exceeded")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))
    }

    fun throwExceptionIfInvalidPerformanceEsopSellOrder(order: Order) {
        val errorList = ArrayList<String>()
        val transactionAmount = order.price * order.quantity
        val user: User = UserRepository.getUser(order.userName)!!

        if (user.getFreePerformanceInventory() < order.quantity)
            errorList.add("Insufficient performance ESOPs in inventory")
        if (user.getFreeMoney() + user.getLockedMoney() + transactionAmount > DataStorage.MAX_AMOUNT)
            errorList.add("Wallet threshold will be exceeded")

        if (errorList.isNotEmpty())
            throw ValidationException(ErrorResponse(errorList))

    }
}