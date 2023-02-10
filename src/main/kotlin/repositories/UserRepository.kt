package repositories

import models.User

object UserRepository {

    private val userList: HashMap<String, User> = HashMap()
    private val registeredEmails = mutableSetOf<String>()
    private val registeredPhoneNumbers = mutableSetOf<String>()
    fun getUser(userName: String) = userList[userName]
    fun addUserInfo(user: User) {
        userList[user.username] = user
        registeredEmails.add(user.emailId)
        registeredPhoneNumbers.add(user.phoneNumber)
    }

    fun checkIfUserExist(userName: String) = userList.contains(userName)
    fun checkIfEmailExist(emailId: String) = registeredEmails.contains(emailId)
    fun checkIfPhoneNumberExists(phoneNumber: String) = registeredPhoneNumbers.contains(phoneNumber)
    fun clearUserList() = userList.clear()
    fun clearEmailList() = registeredEmails.clear()
    fun clearPhoneNumberList() = registeredPhoneNumbers.clear()
    fun getUserListSize() = userList.size
    fun getRegisteredEmailSize() = registeredEmails.size

    fun clearUserRepository() {
        userList.clear()
        registeredEmails.clear()
        registeredPhoneNumbers.clear()
    }
}
