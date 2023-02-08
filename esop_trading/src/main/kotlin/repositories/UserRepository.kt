package repositories

import models.User

open class UserRepository {
    companion object {
        private val userList: HashMap<String, User> = HashMap()
        fun getUser(userName: String) = userList[userName]
        fun checkIfUserExist(userName: String) = userList.contains(userName)
    }
}