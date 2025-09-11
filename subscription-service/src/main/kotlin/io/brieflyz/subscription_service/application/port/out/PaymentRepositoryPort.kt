package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.subscription_service.domain.model.Payment
import io.brieflyz.subscription_service.domain.model.Subscription

interface PaymentRepositoryPort {
    fun save(payment: Payment): Payment
    fun findAllBySubscription(subscription: Subscription): List<Payment>
    fun deleteAllBySubscriptionId(subscriptionId: Long)
}