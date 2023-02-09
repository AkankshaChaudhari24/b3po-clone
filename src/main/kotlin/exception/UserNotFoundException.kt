package exception

import models.ErrorResponse

class UserNotFoundException(val errorResponse: ErrorResponse): Exception() {
}