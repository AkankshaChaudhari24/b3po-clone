package models

data class RegisterResponse(
    val firstName: String? = null,
    val lastName: String? = null,
    val emailID: String? = null,
    val phoneNumber: String? = null,
    val userName: String? = null,
)