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
import models.ErrorResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

@MicronautTest
class TestErrorHandling {

    @Inject
    @field:Client("/")
    lateinit var client: HttpClient

    private val mapper = ObjectMapper().registerModule(
        KotlinModule.Builder().withReflectionCacheSize(512).configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false).configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false).configure(KotlinFeature.StrictNullChecks, false).build()
    )

    @Test
    fun `should get Http 404 for invalid route`() {
        val uri = "/random/url"
        val request = HttpRequest.GET<Any>(uri)

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
        val response = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(HttpStatus.NOT_FOUND, exception.status)
        assertEquals("Invalid URI - $uri", response.error[0])
        assertEquals(1, response.error.size)
    }

    @Test
    fun `should send Http 405 when using get method for user registration`() {
        val uri = "/user/register"
        val request = HttpRequest.GET<Any>(uri)

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
        val response = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, exception.status)
        assertEquals("GET method is not allowed for $uri.", response.error[0])
        assertEquals(1, response.error.size)
    }

    @Test
    fun `should send invalid json when malformed json request is sent`() {
        val request = HttpRequest.POST("/user/register", "{")

        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
        val response = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(1, response.error.size)
        assertEquals("Invalid JSON", response.error[0])
    }

    @Test
    fun `should not accept empty post body`() {
        val request = HttpRequest.POST("/user/register", null)
        val exception = assertThrows(HttpClientResponseException::class.java) {
            client.toBlocking().retrieve(request)
        }
        val response = mapper.readValue(exception.response.body()!!.toString(), ErrorResponse::class.java)

        assertEquals(1, response.error.size)
        assertEquals("Request body cannot be empty", response.error[0])
    }
}