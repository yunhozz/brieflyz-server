package io.brieflyz.subscription_service.common.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class SubscriptionServiceException(val status: ErrorStatus, msg: String) :
    RuntimeException("${status.message} $msg".trim())

class SubscriptionNotFoundException(msg: String) : SubscriptionServiceException(ErrorStatus.SUBSCRIPTION_NOT_FOUND, msg)

class AlreadyUnlimitedPlanException : SubscriptionServiceException(ErrorStatus.ALREADY_UNLIMITED_PLAN_EXCEPTION, "")