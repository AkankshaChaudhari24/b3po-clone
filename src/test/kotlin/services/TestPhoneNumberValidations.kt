package services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestPhoneNumberValidations {
    @Test
    fun `should accept 10 digit phone number`() {
        //Act
        val errorList = Validations.validatePhoneNumber("9307708611")
        //Asset
        assertEquals(0, errorList.size)
    }

    @Test
    fun `should accept 11 digit phone number`() {
        //Act
        val errorList = Validations.validatePhoneNumber("09307708611")
        //Asset
        assertEquals(0, errorList.size)
    }

    @Test
    fun `should accept 12 digit phone number`() {
        //Act
        val errorList = Validations.validatePhoneNumber("919307708611")
        //
        assertEquals(0, errorList.size)
    }

    @Test
    fun `should accept 13 digit phone number`() {
        val errorList = Validations.validatePhoneNumber("+919307708611")
        assertEquals(0, errorList.size)
    }

    @Test
    fun `should accept 14 digit phone number `() {
        val errorList=Validations.validatePhoneNumber("+2139307708611")
        assertEquals(0,errorList.size)
    }

    @Test
    fun `should not accept phone number less than 10 digit`() {
        val errorList = Validations.validatePhoneNumber("9786385")
        assertEquals(1, errorList.size)
        assertEquals("Invalid phone number length.", errorList[0])
    }

    @Test
    fun `should not accept phone number more than 14 digit`() {
        val errorList = Validations.validatePhoneNumber("123451234567890")
        assertEquals(1,errorList.size)
        assertEquals("Invalid phone number length.", errorList[0])
    }

    @Test
    fun `should fail if it's 11 digit and first digit is non-zero`() {
        val errorList = Validations.validatePhoneNumber("19307708611")
        assertEquals(1, errorList.size)
        assertEquals("Trunk code for phone number cannot be non zero.", errorList[0])
    }
    @Test
    fun `should return error if there are characters in phone number`() {
        val errorList = Validations.validatePhoneNumber("0123456789A")
        assertEquals(1, errorList.size)
        assertEquals("Non numerical value other than + found in phone number.", errorList[0])
    }
    @Test
    fun `should return error if country code has characters other than + and digit`() {
        val errorList= Validations.validatePhoneNumber("-091234567890")
        assertEquals(1,errorList.size)
        assertEquals("Invalid country code.",errorList[0])
    }

    @Test
    fun `should return error if country code is wrong but number is valid`() {
        val errorList= Validations.validatePhoneNumber("-1129307708611")
        assertEquals(1,errorList.size)
        assertEquals("Invalid country code.",errorList[0])
    }

    @Test
    fun `return error for trunk code`() {
        val errorList= Validations.validatePhoneNumber("99307708611")
        assertEquals(1,errorList.size)
        assertEquals("Trunk code for phone number cannot be non zero.",errorList[0])

    }
}