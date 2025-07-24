package io.brieflyz.subscription_service.common.exception

import io.brieflyz.core.constants.ErrorStatus

sealed class SubscriptionServiceException(val status: ErrorStatus, msg: String) :
    RuntimeException("${status.message} $msg".trim())

class SubscriptionNotFoundException(msg: String) : SubscriptionServiceException(ErrorStatus.SUBSCRIPTION_NOT_FOUND, msg)

class SubscriptionPlanIdenticalException : SubscriptionServiceException(ErrorStatus.SUBSCRIPTION_PLAN_IDENTICAL, "")

class AlreadyHaveSubscriptionException(msg: String) :
    SubscriptionServiceException(ErrorStatus.ALREADY_HAVE_SUBSCRIPTION, msg)

class AlreadyHaveUnlimitedSubscriptionException(msg: String) :
    SubscriptionServiceException(ErrorStatus.ALREADY_HAVE_UNLIMITED_SUBSCRIPTION, msg)