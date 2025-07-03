package io.brieflyz.api_gateway.exception

import io.brieflyz.core.constants.ErrorCode

sealed class ApiGatewayException(val errorCode: ErrorCode, msg: String) :
    RuntimeException("${errorCode.message} $msg".trim())

class JwtTokenNotExistException : ApiGatewayException(ErrorCode.FORBIDDEN, "")
class JwtTokenNotValidException : ApiGatewayException(ErrorCode.UNAUTHORIZED, "")