package models

class ErrorResponse(val error: ArrayList<String>) {
    constructor(vararg errors: String): this(ArrayList<String>()){
        error.addAll(errors)
    }
}