package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.constants.ErrorCode

sealed class AuthServiceException(val errorCode: ErrorCode, msg: String) :
    RuntimeException("${errorCode.message} $msg".trim())

class UserAlreadyExistsException(msg: String) : AuthServiceException(ErrorCode.USER_ALREADY_EXIST, msg)
class UserNotFoundException(msg: String) : AuthServiceException(ErrorCode.USER_NOT_FOUND, msg)
class UserRegisteredBySocialException : AuthServiceException(ErrorCode.USER_REGISTERED_BY_SOCIAL_LOGIN, "")
class PasswordNotMatchException : AuthServiceException(ErrorCode.PASSWORD_NOT_MATCH, "")
class RefreshTokenNotFoundException : AuthServiceException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "")
class RedisKeyNotExistsException(msg: String) : AuthServiceException(ErrorCode.REDIS_KEY_NOT_FOUND, msg)
class NotAuthorizedRedirectionException(msg: String) : AuthServiceException(ErrorCode.FORBIDDEN, msg)