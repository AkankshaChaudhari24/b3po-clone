package controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import models.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import services.saveUser

@MicronautTest
class
WalletControllerTest {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    private val mapper = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )

    private val walletURI = "/user/user1/addToWallet"

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
    }

    @BeforeEach
    fun setUp() {
        val user = User("user1", "Amy", "Santiago", "9952053438", "amy@gmail.com")
        saveUser(user)
        DataStorage.userList["user1"]!!.addMoneyToWallet(100)
    }

    @Test
    fun shouldNotBeAbleToAddNegativeAmount() {
        val request: HttpRequest<Any> = HttpRequest.POST(walletURI, AddToWalletInput(-10))
        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        val errorResponse: ErrorResponse =
            mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)
        assertEquals("Amount added to wallet has to be positive.", errorResponse.error[0])
    }

    @Test
    fun shouldBeAbleToAddAmount() {
        val amountToBeAdded = 10

        val request: HttpRequest<Any> = HttpRequest.POST(walletURI, AddToWalletInput(amountToBeAdded))
        val res = client.toBlocking().retrieve(request)
        val response: WalletResponse = mapper.readValue(res, WalletResponse::class.java)

        //assertEquals()
        assertEquals("$amountToBeAdded amount added to account", response.message)
        assertEquals(110, DataStorage.userList["user1"]!!.getFreeMoney()) //100 added in setup
    }

    @Test
    fun shouldNotAbleToAddAmountForInvalidUser() {
        val amountToBeAdded = 10
        val request: HttpRequest<Any> = HttpRequest.POST("/user/user2/addToWallet", AddToWalletInput(amountToBeAdded))

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }

        val response: ErrorResponse =
            mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("User does not exist", response.error[0])

    }

    @Test
    fun shouldNotAbleToAddMoreAmountThanSetLimit() {
        val amountToBeAdded = DataStorage.MAX_AMOUNT + 1

        val request: HttpRequest<Any> = HttpRequest.POST(walletURI, AddToWalletInput(amountToBeAdded))

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }

        val response: ErrorResponse =
            mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, exception.status)
        assertEquals(
            "Amount exceeds maximum wallet limit. Wallet range 0 to ${DataStorage.MAX_AMOUNT}",
            response.error[0]
        )
    }
}