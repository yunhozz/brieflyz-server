package io.brieflyz.subscription_service.model.dto

data class SubscriptionResponse(
    val id: Long,
    val memberId: Long,
    val email: String,
    val country: String,
    val city: String,
    val plan: String,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)