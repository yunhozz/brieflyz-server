package io.brieflyz.subscription_service.model.dto

data class SubscriptionResponse(
    val id: Long,
    val memberId: Long,
    val memberEmail: String,
    val subscriptionInterval: String,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)