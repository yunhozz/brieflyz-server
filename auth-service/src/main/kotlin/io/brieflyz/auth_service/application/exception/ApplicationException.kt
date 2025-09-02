package io.brieflyz.auth_service.application.exception

import io.brieflyz.auth_service.presentation.exception.AuthServiceException
import io.brieflyz.core.constants.ErrorStatus

sealed class ApplicationException(status: ErrorStatus, msg: String) :
    AuthServiceException(status, "${status.message} $msg".trim())

class RefreshTokenNotFoundException : ApplicationException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND, "")