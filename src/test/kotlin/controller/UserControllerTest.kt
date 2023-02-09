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
import models.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import repositories.OrderRepository
import repositories.UserRepository
import services.saveUser


@MicronautTest
class UserControllerTest {


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

    private val registerURI = "/user/register"

    @AfterEach
    fun tearDown() {
        UserRepository.clearUserList()
        UserRepository.clearPhoneNumberList()
        UserRepository.clearEmailList()
        OrderRepository.clearBuyList()
        OrderRepository.clearSellList()
        OrderRepository.clearPerformanceSellList()
        OrderRepository.setOrderId(1L)
        OrderRepository.setOrderExecutionId(1L)
    }

    @Test
    fun shouldRegisterAValidUser() {
        val registerInput = RegisterInput(
            userName = "amy_santiago",
            firstName = "Amy",
            lastName = "Santiago",
            phoneNumber = "1234567890",
            emailID = "amy@gmail.com"
        )
        if (this::client.isInitialized) {
            val request = HttpRequest.POST(registerURI, registerInput)
            val responseString = client.toBlocking().retrieve(request)
            val response: RegisterResponse = mapper.readValue(responseString, RegisterResponse::class.java)
            assertEquals("amy_santiago", response.userName)
            assertEquals("Amy", response.firstName)
            assertEquals("Santiago", response.lastName)
            assertEquals("1234567890", response.phoneNumber)
            assertEquals("amy@gmail.com", response.emailID)
        }
    }

    @Test
    fun shouldThrowErrorsForInvalidUser() {
        val registerInput = RegisterInput()
        val request = HttpRequest.POST(registerURI, registerInput)
        if (this::client.isInitialized) {
            val exception = assertThrows<HttpClientResponseException> {
                client.toBlocking().retrieve(request)
            }
            assertEquals(HttpStatus.BAD_REQUEST, exception.status)

            val errorResponse = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

            assertEquals(5, errorResponse.error.size)
            assertEquals("firstName is missing.", errorResponse.error[0])
            assertEquals("lastName is missing.", errorResponse.error[1])
            assertEquals("phoneNumber is missing.", errorResponse.error[2])
            assertEquals("emailID is missing.", errorResponse.error[3])
            assertEquals("userName is missing.", errorResponse.error[4])
        }
    }

    @Test
    fun registerNewUserWhileOtherUsersExist() {
        val user1 = User(
            firstName = "user1",
            lastName = "user1",
            emailId = "user1@example.com",
            phoneNumber = "+911234567891",
            username = "user1"
        )
        val user2 = User(
            firstName = "user2",
            lastName = "user2",
            emailId = "user2@example.com",
            phoneNumber = "+911234567892",
            username = "user2"
        )
        val user3 = User(
            firstName = "user3",
            lastName = "user3",
            emailId = "user3@example.com",
            phoneNumber = "+911234567893",
            username = "user3"
        )
        saveUser(user1)
        saveUser(user2)
        saveUser(user3)
        assertEquals(3, UserRepository.getUserListSize())

    }
}