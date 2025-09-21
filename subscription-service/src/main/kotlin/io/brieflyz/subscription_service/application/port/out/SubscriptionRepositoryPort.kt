package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.subscription_service.domain.model.Subscription

interface SubscriptionRepositoryPort {
    fun save(subscription: Subscription): Subscription
    fun findById(subscriptionId: Long): Subscription?
    fun findByEmail(email: String): Subscription?
    fun softDeleteInIdsQuery(subscriptionIds: List<Long>)
    fun deleteById(subscriptionId: Long)
}