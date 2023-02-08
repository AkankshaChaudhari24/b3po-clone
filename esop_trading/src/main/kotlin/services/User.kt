package services

import models.DataStorage
import models.User

fun saveUser(user: User) {
    DataStorage.userList[user.username] = user
    DataStorage.registeredEmails.add(user.emailId)
    DataStorage.registeredPhoneNumbers.add(user.phoneNumber)
}