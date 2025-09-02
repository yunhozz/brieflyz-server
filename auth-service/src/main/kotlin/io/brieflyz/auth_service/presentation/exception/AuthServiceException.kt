package io.brieflyz.auth_service.presentation.exception

import io.brieflyz.core.constants.ErrorStatus

abstract class AuthServiceException(val status: ErrorStatus, message: String) : RuntimeException(message)