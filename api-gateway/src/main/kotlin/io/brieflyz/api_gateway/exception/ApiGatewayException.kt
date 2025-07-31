package io.brieflyz.api_gateway.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class ApiGatewayException(val status: ErrorStatus, msg: String) :
    RuntimeException("${status.message} $msg".trim())

class JwtTokenNotExistException : ApiGatewayException(ErrorStatus.FORBIDDEN, "")
class JwtTokenNotValidException : ApiGatewayException(ErrorStatus.UNAUTHORIZED, "")