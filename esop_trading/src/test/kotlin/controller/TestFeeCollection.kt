package controller

import io.micronaut.http.HttpRequest
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import models.DataStorage
import models.FeeResponse
import models.User
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.saveUser
import java.math.BigInteger

@MicronautTest
class TestFeeCollection {
    @Inject
    @field:Client("/")
    lateinit var client: HttpClient
    @BeforeEach
    fun setUp() {
        val buyer = User("jake", "Jake", "Peralta", "9844427549", "jake@gmail.com") //Buyer
        buyer.addMoneyToWallet(10000)
        val seller = User("amy", "Amy", "Santiago", "9472919384", "amy@gmail.com") //Seller
        seller.addEsopToInventory(100, "NON-PERFORMANCE")
        seller.addEsopToInventory(100, "PERFORMANCE")
        saveUser(buyer)
        saveUser(seller)
    }

    @AfterEach
    fun tearDown() {
        DataStorage.userList.clear()
        DataStorage.registeredEmails.clear()
        DataStorage.registeredPhoneNumbers.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.orderId = 1L
        DataStorage.orderExecutionId = 1L
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
        DataStorage.userList["jake"]!!.addOrderToExecutionQueue(1,"BUY", 100)
        DataStorage.userList["amy"]!!.addOrderToExecutionQueue(1,"SELL",100)
        val request = HttpRequest.GET<FeeResponse>("/fees")

        val response = client.toBlocking().retrieve(request, FeeResponse::class.java)

        assertEquals(BigInteger.valueOf(2L), response.totalFees)
    }

    @Test
    fun `total fee should be rounded and not floored`() {
        DataStorage.userList["jake"]!!.addOrderToExecutionQueue(1,"BUY", 30)
        DataStorage.userList["amy"]!!.addOrderToExecutionQueue(1,"SELL",30)
        val request = HttpRequest.GET<FeeResponse>("/fees")

        val response = client.toBlocking().retrieve(request, FeeResponse::class.java)

        assertEquals(BigInteger.valueOf(1L), response.totalFees)
    }
}