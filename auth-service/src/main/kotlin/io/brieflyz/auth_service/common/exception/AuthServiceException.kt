package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class AuthServiceException(val status: ErrorStatus, msg: String) :
    RuntimeException("${status.message} $msg".trim())

class UserAlreadyExistsException(msg: String) : AuthServiceException(ErrorStatus.USER_ALREADY_EXIST, msg)
class UserNotFoundException(msg: String) : AuthServiceException(ErrorStatus.USER_NOT_FOUND, msg)
class UserRegisteredBySocialException : AuthServiceException(ErrorStatus.USER_REGISTERED_BY_SOCIAL_LOGIN, "")
class PasswordNotMatchException : AuthServiceException(ErrorStatus.PASSWORD_NOT_MATCH, "")
class RefreshTokenNotFoundException : AuthServiceException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND, "")
class RedisKeyNotExistsException(msg: String) : AuthServiceException(ErrorStatus.REDIS_KEY_NOT_FOUND, msg)