package constants

class PhoneNumber {
    companion object {
        const val TRUNK_ERROR_MESSAGE = "Trunk code for phone number cannot be non zero."
        const val NON_NUMERICAL_ERROR_MESSAGE = "Non numerical value other than + found in phone number."
        const val COUNTRY_CODE_ERROR_MESSAGE = "Invalid country code."
        const val INVALID_LENGTH_ERROR_MESSAGE ="Invalid phone number length."
        const val ALREADY_EXISTS_ERROR_MESSAGE ="Phone number already exists."
    }
}