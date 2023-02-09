package controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import models.DataStorage
import models.FeeResponse
import models.Order
import models.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import repositories.OrderRepository
import repositories.UserRepository
import services.OrderServices
import services.saveUser
import java.math.BigInteger

@MicronautTest
class TestFeeCollection {
    @Inject
    @field:Client("/")
    lateinit var client: HttpClient
    lateinit var order1: Order
    lateinit var order2: Order

    @BeforeEach
    fun setUp() {
        val buyer = User("jake", "Jake", "Peralta", "9844427549", "jake@gmail.com") //Buyer
        buyer.addMoneyToWallet(10000)
        val seller = User("amy", "Amy", "Santiago", "9472919384", "amy@gmail.com") //Seller
        seller.addEsopToInventory(100, "NON-PERFORMANCE")
        seller.addEsopToInventory(100, "PERFORMANCE")
        saveUser(buyer)
        saveUser(seller)
        order1 = Order(userName = buyer.username, quantity = 1, price = 100, type = "BUY")
        order2 = Order(userName = seller.username, quantity = 1, price = 100, type = "SELL")

    }

    @AfterEach
    fun tearDown() {
        UserRepository.clearUserList()
        UserRepository.clearEmailList()
        UserRepository.clearPhoneNumberList()
        OrderRepository.clearBuyList()
        OrderRepository.clearSellList()
        OrderRepository.clearPerformanceSellList()
        OrderRepository.setOrderId(1L)
        OrderRepository.setOrderExecutionId(1L)
        DataStorage.TOTAL_FEE_COLLECTED = BigInteger.valueOf(0)
    }

    @Test
    fun `total fee is initially zero`() {
        val request = HttpRequest.GET<FeeResponse>("/fees")

        val response = client.toBlocking().retrieve(request, FeeResponse::class.java)

        assertEquals(BigInteger.valueOf(0L), response.totalFees)
    }

    @Test
    fun `total fee should be 2 percent of total transaction`() {
        OrderRepository.addOrder(order1)
        OrderRepository.addOrder(order2)
        OrderServices.matchOrders()
        val request = HttpRequest.GET<FeeResponse>("/fees")

        val response = client.toBlocking().retrieve(request, FeeResponse::class.java)

        assertEquals(BigInteger.valueOf(2L), response.totalFees)
    }

    @Test
    fun `total fee should be rounded and not floored`() {
        OrderRepository.addOrder(order1)
        OrderRepository.addOrder(order2)
        OrderServices.matchOrders()
        val request = HttpRequest.GET<FeeResponse>("/fees")

        val response = client.toBlocking().retrieve(request, FeeResponse::class.java)

        assertEquals(BigInteger.valueOf(2L), response.totalFees)
    }
}