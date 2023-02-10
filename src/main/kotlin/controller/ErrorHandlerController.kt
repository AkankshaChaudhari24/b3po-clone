package controller

import com.fasterxml.jackson.core.JsonParseException
import exception.UserNotFoundException
import exception.ValidationException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.web.router.exceptions.UnsatisfiedBodyRouteException
import models.ErrorResponse

@Controller("/")
class ErrorHandlerController {
    @Error(global = true, exception = JsonParseException::class)
    fun handleInvalidJson(): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse("Invalid JSON"))
    }

    @Error(global = true, exception = UnsatisfiedBodyRouteException::class)
    fun handleEmptyBody(): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(ErrorResponse("Request body cannot be empty"))
    }

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    fun handleInvalidRoute(request: HttpRequest<*>): HttpResponse<ErrorResponse> {
        return HttpResponse.notFound(ErrorResponse("Invalid URI - ${request.uri}"))
    }

    @Error(global = true, exception = UserNotFoundException::class)
    fun handleUserNotFound(exception: UserNotFoundException): HttpResponse<ErrorResponse>{
        return HttpResponse.notFound(exception.errorResponse)
    }
    @Error(global = true, status = HttpStatus.METHOD_NOT_ALLOWED)
    fun handleWrongHttpMethod(request: HttpRequest<*>): HttpResponse<ErrorResponse> {
        return HttpResponse.notAllowed<ErrorResponse>()
            .body(ErrorResponse("${request.method} method is not allowed for ${request.uri}."))
    }

    @Error(global = true, exception = ValidationException::class)
    fun handleCustomErrors(exception: ValidationException): HttpResponse<ErrorResponse> {
        return HttpResponse.badRequest(exception.errorResponse)
    }
}