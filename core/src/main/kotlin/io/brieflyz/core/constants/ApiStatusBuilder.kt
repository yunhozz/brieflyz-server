package io.brieflyz.core.constants

object ApiStatusBuilder {
    infix fun Int.ok(message: String) = ApiStatus(this, message)
    infix fun Int.created(message: String) = ApiStatus(this, message)
    infix fun Int.noContent(message: String) = ApiStatus(this, message)
    infix fun Int.badRequest(message: String) = ApiStatus(this, message)
    infix fun Int.unauthorized(message: String) = ApiStatus(this, message)
    infix fun Int.forbidden(message: String) = ApiStatus(this, message)
    infix fun Int.notFound(message: String) = ApiStatus(this, message)
    infix fun Int.methodNotAllowed(message: String) = ApiStatus(this, message)
    infix fun Int.conflict(message: String) = ApiStatus(this, message)
    infix fun Int.internalServerError(message: String) = ApiStatus(this, message)
    infix fun Int.serviceUnavailable(message: String) = ApiStatus(this, message)

    data class ApiStatus(val statusCode: Int, val message: String)
}