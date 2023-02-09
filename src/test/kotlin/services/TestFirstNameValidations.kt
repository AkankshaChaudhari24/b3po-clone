package services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import services.Validations.Companion.validateFirstName

class TestFirstNameValidations {
    @Test
    fun `should accept first name with spaces`(){
       val errorList = validateFirstName("Jake Peralta")

        assert(errorList.isEmpty())
    }

    @Test
    fun `first name cannot contain digits`(){
        val errorList = validateFirstName("123")

        assertEquals(1, errorList.size)
        assertEquals("Invalid first name. First name should only contain characters and cannot have more than one continuous space.", errorList[0])
    }

    @Test
    fun `first name cannot be less than three characters`(){
        val errorList = validateFirstName("aa")

        assertEquals(1, errorList.size)
        assertEquals("First name has to be at least three characters.", errorList[0])
    }

    @Test
    fun `first name cannot contain continuous spaces`() {
        val errorList = validateFirstName("Jake  Peralt   a")

        assertEquals(1, errorList.size)
        assertEquals("Invalid first name. First name should only contain characters and cannot have more than one continuous space.", errorList[0])
    }

    @Test
    fun `first name can contain other languages`() {
        val errorList = validateFirstName("月が綺麗ですね")

        assert(errorList.isEmpty())
    }

    @Test
    fun `first name can contain mark characters`(){
        val errorList = validateFirstName("नमस्ते")

        assert(errorList.isEmpty())
    }
    @Test
    fun `first name cannot be null`() {
        val errorList = validateFirstName(null)

        assertEquals(1, errorList.size)
        assertEquals("firstName is missing.", errorList[0])
    }


}