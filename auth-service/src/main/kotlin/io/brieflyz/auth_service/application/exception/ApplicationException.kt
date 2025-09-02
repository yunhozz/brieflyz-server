package io.brieflyz.auth_service.application.exception

import io.brieflyz.auth_service.presentation.exception.AuthServiceException
import io.brieflyz.core.constants.ErrorStatus

sealed class ApplicationException(status: ErrorStatus, msg: String) :
    AuthServiceException(status, "${status.message} $msg".trim())

class UserAlreadyExistsException(msg: String) : ApplicationException(ErrorStatus.USER_ALREADY_EXIST, msg)
class UserNotFoundException(msg: String) : ApplicationException(ErrorStatus.USER_NOT_FOUND, msg)
class UserRegisteredBySocialException : ApplicationException(ErrorStatus.USER_REGISTERED_BY_SOCIAL_LOGIN, "")
class PasswordNotMatchException : ApplicationException(ErrorStatus.PASSWORD_NOT_MATCH, "")
class RefreshTokenNotFoundException : ApplicationException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND, "")