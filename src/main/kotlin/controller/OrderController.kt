package controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import models.Order
import models.OrderRequest
import models.OrderResponse
import repositories.OrderRepository
import services.OrderServices
import validations.OrderValidations

@Controller("/user")
class OrderController {
    @Post("/{userName}/createOrder")
    fun createOrder(@Body orderRequest: OrderRequest, @PathVariable userName: String): HttpResponse<OrderResponse> {
        val orderValidations = OrderValidations()
        orderValidations.validateNullOrderFields(orderRequest, userName)
        orderValidations.validateInvalidEsopAndOrderType(orderRequest)
        val order = Order(
            userName = userName,
            quantity = orderRequest.quantity,
            type = orderRequest.type,
            price = orderRequest.price,
            esopType = orderRequest.esopType
        )
        OrderRepository.addOrder(order)
        OrderServices.matchOrders()
        return HttpResponse.ok(OrderResponse(order.quantity, order.type, order.price))
    }
}