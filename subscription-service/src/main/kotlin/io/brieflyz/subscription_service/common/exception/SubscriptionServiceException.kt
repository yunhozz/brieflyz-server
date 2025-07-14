package io.brieflyz.subscription_service.common.exception

import io.brieflyz.core.constants.ErrorCode

sealed class SubscriptionServiceException(val errorCode: ErrorCode, msg: String) :
    RuntimeException("${errorCode.message} $msg".trim())

class SubscriptionNotFoundException(msg: String) :
    SubscriptionServiceException(ErrorCode.SUBSCRIPTION_NOT_FOUND, msg)

class InvalidSubscriptionIntervalException(msg: String) :
    SubscriptionServiceException(ErrorCode.INVALID_SUBSCRIPTION_INTERVAL, msg)