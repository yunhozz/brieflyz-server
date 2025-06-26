package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.dto.api.ErrorCode

sealed class CustomException(val errorCode: ErrorCode, msg: String) :
    RuntimeException("${errorCode.message} $msg".trim())

class UserAlreadyExistsException(msg: String) : CustomException(ErrorCode.USER_ALREADY_EXIST, msg)
class UserNotFoundException(msg: String) : CustomException(ErrorCode.USER_NOT_FOUND, msg)
class PasswordNotMatchException : CustomException(ErrorCode.PASSWORD_NOT_MATCH, "")
class RefreshTokenNotFoundException : CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "")
class RedisKeyNotExistsException(msg: String) : CustomException(ErrorCode.REDIS_KEY_NOT_FOUND, msg)