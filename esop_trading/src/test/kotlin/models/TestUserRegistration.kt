package models

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import services.saveUser

class TestUserRegistration {
    @AfterEach
    fun tearDown(){
        DataStorage.userList.clear()
        DataStorage.registeredEmails.clear()
        DataStorage.registeredPhoneNumbers.clear()
        DataStorage.buyList.clear()
        DataStorage.sellList.clear()
        DataStorage.performanceSellList.clear()
        DataStorage.orderId = 1
        DataStorage.orderExecutionId = 1
    }

    @Test
    fun `can create new valid user`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        saveUser(user)

        assertEquals(1, DataStorage.userList.size)
        assertEquals(user, DataStorage.userList["user"])
    }

    @Test
    fun `can create multiple users`(){
        val user1 = User(firstName = "user1", lastName = "user1", emailId = "user1@example.com", phoneNumber = "+911234567891", username = "user1")
        val user2 = User(firstName = "user2", lastName = "user2", emailId = "user2@example.com", phoneNumber = "+911234567892", username = "user2")
        val user3 = User(firstName = "user3", lastName = "user3", emailId = "user3@example.com", phoneNumber = "+911234567893", username = "user3")
        saveUser(user1)
        saveUser(user2)
        saveUser(user3)

        assertEquals(3, DataStorage.userList.size)
        assertEquals(user1, DataStorage.userList["user1"])
        assertEquals(user2, DataStorage.userList["user2"])
        assertEquals(user3, DataStorage.userList["user3"])
    }

    @Test
    fun `user email added to registered email list`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        saveUser(user)

        assertEquals(1, DataStorage.registeredEmails.size)
        assert(DataStorage.registeredEmails.contains("user@example.com"))
    }

    @Test
    fun `user phone number added to registered phone number list`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")
        saveUser(user)

        assertEquals(1, DataStorage.registeredEmails.size)
        assert(DataStorage.registeredPhoneNumbers.contains("+911234567890"))
    }

    @Test
    fun `user wallet and inventory are initially empty`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        assertEquals(0, user.getFreeMoney())
        assertEquals(0, user.getLockedMoney())
        assertEquals(0, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
    }

    @Test
    fun `can add money to wallet`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.addMoneyToWallet(100)

        assertEquals(100, user.getFreeMoney())
        assertEquals(0, user.getLockedMoney())
    }

    @Test
    fun `can add normal ESOPs to inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.addEsopToInventory(10)

        assertEquals(10, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
    }

    @Test
    fun `can add performance ESOPs to inventory`(){
        val user = User(firstName = "user", lastName = "user", emailId = "user@example.com", phoneNumber = "+911234567890", username = "user")

        user.addEsopToInventory(10, "PERFORMANCE")

        assertEquals(10, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
        assertEquals(0, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
    }
}