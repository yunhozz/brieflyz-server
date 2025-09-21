package io.brieflyz.subscription_service.application.port.`in`

import io.brieflyz.subscription_service.application.dto.command.CreateSubscriptionCommand

interface CreateSubscriptionUseCase {
    fun create(command: CreateSubscriptionCommand): Long
}

interface CancelSubscriptionUseCase {
    fun cancel(subscriptionId: Long): Long
}

interface DeleteSubscriptionUseCase {
    fun delete(subscriptionId: Long)
}