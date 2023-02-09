package validations

import constants.PhoneNumber
import models.RegisterInput
import repositories.UserRepository

class Validations {
    companion object {
        fun validateRegisterInput(body: RegisterInput): ArrayList<String> {
            val errorList = arrayListOf<String>()

            val firstName: String? = body.firstName?.trim()
            val lastName: String? = body.lastName?.trim()
            val phoneNumber: String? = body.phoneNumber?.trim()
            val email: String? = body.emailID?.trim()
            val username: String? = body.userName?.trim()

            errorList.addAll(validateFirstName(firstName))
            errorList.addAll(validateLastName(lastName))
            errorList.addAll(validatePhoneNumber(phoneNumber))
            errorList.addAll(validateEmailIds(email))
            errorList.addAll(validateUserName(username))

            return errorList
        }

        fun validateFirstName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (name == null) {
                errorList.add("firstName is missing.")
                return errorList
            }
            if (name.length < 3)
                errorList.add("First name has to be at least three characters.")

            if (!name.matches(Regex("([\\p{L}\\p{M}]+ ?)+")))
                errorList.add("Invalid first name. First name should only contain characters and cannot have more than one continuous space.")

            return errorList
        }

        fun validateLastName(name: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (name == null) {
                errorList.add("lastName is missing.")
                return errorList
            }

            if (name.isEmpty())
                errorList.add("Last name cannot be empty.")

            if (!name.matches(Regex("([\\p{L}\\p{M}]+ ?)*")))
                errorList.add("Invalid last name. Last name should only contain characters and cannot have more than one continuous space.")

            return errorList
        }

        fun validateUserName(username: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (username == null) {
                errorList.add("userName is missing.")
                return errorList
            }

            if (UserRepository.checkIfUserExist(username))
                errorList.add("Username already taken")

            if (username.length < 3)
                errorList.add("Username has to be at least three characters.")

            if (!username.matches(Regex("[_\\d]*[\\p{L}\\p{M}][\\w_]*")))
                errorList.add("Username can only contain characters,numbers and underscores and must have at least one character.")

            return errorList

        }

        fun validateUser(userName: String): Boolean {
            if (UserRepository.checkIfUserExist(userName)) {
                return true
            }
            return false
        }

        fun validateEmailIds(emailId: String?): Collection<String> {
            val errorList = mutableSetOf<String>()
            if (emailId == null) {
                errorList.add("emailID is missing.")
                return errorList
            }


            if (UserRepository.checkIfEmailExist(emailId)) {
                errorList.add("Email already exists")
            }
            if (emailId.first() == '.' || emailId.first() == '-' || emailId.last() == '.' || emailId.last() == '-') {
                errorList.add("Invalid Email address")
            }
            val hyphen = "--"
            val dots = ".."
            if (emailId.contains(dots)) {
                errorList.add("Invalid Email address")
            }
            if (!emailId.matches(Regex("^[\\\\a-zA-Z0-9.!#\$%&'*+/=?^_`{|}\" ~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+\$"))) {
                errorList.add("Invalid Email address")
            } else {
                val splitMail = emailId.split('@')
                val username = splitMail[0]
                val domain = splitMail[1]
                if (!validDomain(domain)) {
                    errorList.add("Invalid Email address")
                }
                if (username.contains(hyphen)) {
                    errorList.add("Invalid Email address")
                }
                if (emailId.length > 255) {
                    errorList.add("Email must be less than 255 characters")
                }
                if (splitMail[0].last() == '.') {
                    errorList.add("Invalid Email address")
                }
                if (splitMail[0].length > 64) {
                    errorList.add("User name of email must be less than 64 characters long")
                }
                if (splitMail[1].length > 255) {
                    errorList.add("Domain name must be less than 255 characters long")
                }
                val subdomain = splitMail[1].split('.')
                if (subdomain[1].length <= 1) {
                    errorList.add("Subdomain should be more than 1 characters long ")
                }
            }

            return errorList.toList()
        }

        fun validatePhoneNumber(phoneNumber: String?): ArrayList<String> {
            val errorList = arrayListOf<String>()
            if (phoneNumber == null) {
                errorList.add("phoneNumber is missing.")
                return errorList
            }
            if (UserRepository.checkIfPhoneNumberExists(phoneNumber)) {
                errorList.add(PhoneNumber.ALREADY_EXISTS_ERROR_MESSAGE)
                return errorList
            }
            if (phoneNumber.length < 10 || phoneNumber.length > 14) {
                errorList.add(PhoneNumber.INVALID_LENGTH_ERROR_MESSAGE)
                return errorList
            }
            val code = countryCode(phoneNumber)
            if (code.length == 1 && code[0] != '0') {
                errorList.add(PhoneNumber.TRUNK_ERROR_MESSAGE)
                return errorList
            }
            if (code.isNotEmpty() && !code.matches(Regex("\\+?\\d*"))) {
                errorList.add(PhoneNumber.COUNTRY_CODE_ERROR_MESSAGE)
                return errorList
            }
            val number = phoneNumber.subSequence(code.length, phoneNumber.length)
            if (!number.matches(Regex("\\d*"))) {
                errorList.add(PhoneNumber.NON_NUMERICAL_ERROR_MESSAGE)
            }
            return errorList
        }

        private fun countryCode(phoneNumber: String): String {
            if (phoneNumber.length == 10 || (phoneNumber.length == 11 && phoneNumber[0] == '0'))
                return ""
            return phoneNumber.subSequence(0, phoneNumber.length - 10).toString()


        }

        private fun validDomain(domain: String): Boolean {
            val labels = domain.split('.')
            return labels.all {
                validLabel(it)
            }
        }

        private fun validLabel(label: String): Boolean {
            val ldhStrRegex = "[a-zA-Z0-9-]+"
            val labelRegex = "[a-zA-Z]$ldhStrRegex[a-zA-Z0-9]"
            return label.matches(Regex(labelRegex))
        }
    }
}