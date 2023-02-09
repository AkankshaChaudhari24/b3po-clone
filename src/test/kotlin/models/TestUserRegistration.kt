package models

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import repositories.OrderRepository
import repositories.UserRepository
import services.saveUser

class TestUserRegistration {
    @AfterEach
    fun tearDown() {
        UserRepository.clearUserList()
        UserRepository.clearEmailList()
        UserRepository.clearPhoneNumberList()

        OrderRepository.clearBuyList()
        OrderRepository.clearSellList()
        OrderRepository.clearPerformanceSellList()
        OrderRepository.setOrderId(1L)
        OrderRepository.setOrderExecutionId(1L)
    }

    @Test
    fun `can create new valid user`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )
        saveUser(user)

        assertEquals(1, UserRepository.getUserListSize())
        assertEquals(user, UserRepository.getUser("user"))
    }

    @Test
    fun `can create multiple users`() {
        val user1 = User(
            firstName = "user1",
            lastName = "user1",
            emailId = "user1@example.com",
            phoneNumber = "+911234567891",
            username = "user1"
        )
        val user2 = User(
            firstName = "user2",
            lastName = "user2",
            emailId = "user2@example.com",
            phoneNumber = "+911234567892",
            username = "user2"
        )
        val user3 = User(
            firstName = "user3",
            lastName = "user3",
            emailId = "user3@example.com",
            phoneNumber = "+911234567893",
            username = "user3"
        )
        saveUser(user1)
        saveUser(user2)
        saveUser(user3)

        assertEquals(3, UserRepository.getUserListSize())
        assertEquals(user1, UserRepository.getUser("user1"))
        assertEquals(user2, UserRepository.getUser("user2"))
        assertEquals(user3, UserRepository.getUser("user3"))
    }

    @Test
    fun `user email added to registered email list`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )
        saveUser(user)

        assertEquals(1, UserRepository.getRegisteredEmailSize())
        assert(UserRepository.checkIfEmailExist("user@example.com"))
    }

    @Test
    fun `user phone number added to registered phone number list`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )
        saveUser(user)

        assertEquals(1, UserRepository.getRegisteredEmailSize())
        assert(UserRepository.checkIfPhoneNumberExists("+911234567890"))
    }

    @Test
    fun `user wallet and inventory are initially empty`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )

        assertEquals(0, user.getFreeMoney())
        assertEquals(0, user.getLockedMoney())
        assertEquals(0, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
    }

    @Test
    fun `can add money to wallet`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )

        user.addMoneyToWallet(100)

        assertEquals(100, user.getFreeMoney())
        assertEquals(0, user.getLockedMoney())
    }

    @Test
    fun `can add normal ESOPs to inventory`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )

        user.addEsopToInventory(10)

        assertEquals(10, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
        assertEquals(0, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
    }

    @Test
    fun `can add performance ESOPs to inventory`() {
        val user = User(
            firstName = "user",
            lastName = "user",
            emailId = "user@example.com",
            phoneNumber = "+911234567890",
            username = "user"
        )

        user.addEsopToInventory(10, "PERFORMANCE")

        assertEquals(10, user.getFreePerformanceInventory())
        assertEquals(0, user.getLockedPerformanceInventory())
        assertEquals(0, user.getFreeInventory())
        assertEquals(0, user.getLockedInventory())
    }
}