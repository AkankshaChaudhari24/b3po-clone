package validations

import exception.ValidationException
import models.ErrorResponse
import models.OrderRequest

class OrderValidations {
    fun validateNullOrderFields(order: OrderRequest, userName: String) {
        val errorMessages: ArrayList<String> = ArrayList()
        if (!Validations.validateUser(userName)) {
            errorMessages.add("userName does not exists.")
        }
        if (order.type.isBlank()) {
            errorMessages.add("type is missing, type should be BUY or SELL.")
        }
        if (order.type == "SELL" && order.esopType.isNullOrBlank()) {
            errorMessages.add("esopType is missing, SELL order requires esopType.")
        }
        if (order.price <= 0) {
            errorMessages.add("price is missing, price should be positive")
        }
        if (errorMessages.isNotEmpty()) {
            throw ValidationException(ErrorResponse(errorMessages))
        }

    }

    fun validateInvalidEsopAndOrderType(order: OrderRequest) {
        val errorMessages: ArrayList<String> = ArrayList()
        if (order.type !in arrayOf("BUY", "SELL")) {
            errorMessages.add("Invalid order type.")
        }

        if (order.type == "SELL" && order.esopType !in arrayOf("PERFORMANCE", "NON-PERFORMANCE")) {
            errorMessages.add("Invalid type of ESOP, ESOP type should be PERFORMANCE or NON-PERFORMANCE.")
        }

        if (errorMessages.isNotEmpty()) {
            throw ValidationException(ErrorResponse(errorMessages))
        }
    }

}