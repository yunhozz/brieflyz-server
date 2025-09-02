package io.brieflyz.auth_service.domain.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class DomainException(val status: ErrorStatus, message: String) :
    RuntimeException("${status.message} $message".trim())

class UserAlreadyExistsException(msg: String) : DomainException(ErrorStatus.USER_ALREADY_EXIST, msg)
class UserNotFoundException(msg: String) : DomainException(ErrorStatus.USER_NOT_FOUND, msg)
class UserRegisteredBySocialException : DomainException(ErrorStatus.USER_REGISTERED_BY_SOCIAL_LOGIN, "")
class PasswordNotMatchException : DomainException(ErrorStatus.PASSWORD_NOT_MATCH, "")