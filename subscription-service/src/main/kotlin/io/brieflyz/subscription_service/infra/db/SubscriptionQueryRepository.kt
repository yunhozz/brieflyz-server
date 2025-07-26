package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.dto.response.SubscriptionQuery

interface SubscriptionQueryRepository {
    fun findWithPaymentsByIdQuery(id: Long): SubscriptionQuery?
}