package services
import models.User
import repositories.UserRepository

fun saveUser(user: User) {
    UserRepository.addUserInfo(user)
}