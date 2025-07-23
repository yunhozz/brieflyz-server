package io.brieflyz.subscription_service.common.exception

import io.brieflyz.core.constants.ErrorCode

sealed class SubscriptionServiceException(val errorCode: ErrorCode, msg: String) :
    RuntimeException("${errorCode.message} $msg".trim())

class SubscriptionNotFoundException(msg: String) : SubscriptionServiceException(ErrorCode.SUBSCRIPTION_NOT_FOUND, msg)

class AlreadyUnlimitedPlanException : SubscriptionServiceException(ErrorCode.ALREADY_UNLIMITED_PLAN_EXCEPTION, "")