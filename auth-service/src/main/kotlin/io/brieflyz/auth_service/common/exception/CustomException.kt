package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.dto.api.ErrorCode

sealed class CustomException(val errorCode: ErrorCode, msg: String?) : RuntimeException(
    if (msg.isNullOrBlank()) errorCode.message else "${errorCode.message} $msg"
)

class TestException(msg: String? = null) : CustomException(ErrorCode.INTERNAL_SERVER_ERROR, msg)