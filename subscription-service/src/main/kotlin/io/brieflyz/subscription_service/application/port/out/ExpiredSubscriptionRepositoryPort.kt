package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.subscription_service.domain.model.ExpiredSubscription

interface ExpiredSubscriptionRepositoryPort {
    fun saveAll(expiredSubscriptions: List<ExpiredSubscription>)
    fun deleteAll()
}