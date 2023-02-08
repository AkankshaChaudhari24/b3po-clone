package exception

import models.ErrorResponse

class ValidationException(val errorResponse: ErrorResponse): Exception() {

}