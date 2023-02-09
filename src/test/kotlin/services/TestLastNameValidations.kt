package services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import validations.Validations.Companion.validateLastName

class TestLastNameValidations {
    @Test
    fun `should accept last name with spaces`(){
       val errorList = validateLastName("Jake Peralta")

        assert(errorList.isEmpty())
    }

    @Test
    fun `last name cannot contain digits`(){
        val errorList = validateLastName("123")

        assertEquals(1, errorList.size)
        assertEquals("Invalid last name. Last name should only contain characters and cannot have more than one continuous space.", errorList[0])
    }

    @Test
    fun `last name cannot contain continuous spaces`() {
        val errorList = validateLastName("Jake  Peralt   a")

        assertEquals(1, errorList.size)
        assertEquals("Invalid last name. Last name should only contain characters and cannot have more than one continuous space.", errorList[0])
    }

    @Test
    fun `last name can contain other languages`() {
        val errorList = validateLastName("月が綺麗ですね")

        assert(errorList.isEmpty())
    }

    @Test
    fun `last name can contain mark characters`(){
        val errorList = validateLastName("नमस्ते")

        assert(errorList.isEmpty())
    }
    @Test
    fun `last name cannot be null`() {
        val errorList = validateLastName(null)

        assertEquals(1, errorList.size)
        assertEquals("lastName is missing.", errorList[0])
    }

    @Test
    fun `last name should contain at least one character`(){
        val errorList = validateLastName("")

        assertEquals(1, errorList.size)
        assertEquals("Last name cannot be empty.", errorList[0])
    }
}