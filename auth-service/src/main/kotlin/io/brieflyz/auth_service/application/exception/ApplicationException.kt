package io.brieflyz.auth_service.application.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class ApplicationException(val status: ErrorStatus, message: String) :
    RuntimeException("${status.message} $message".trim())

class RefreshTokenNotFoundException : ApplicationException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND, "")