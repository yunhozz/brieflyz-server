package io.brieflyz.subscription_service.domain.model

class ExpiredSubscription(
    val id: Long,
    val email: String,
    val plan: String
)