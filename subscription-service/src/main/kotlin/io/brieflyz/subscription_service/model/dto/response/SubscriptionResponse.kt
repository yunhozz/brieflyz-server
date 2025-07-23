package io.brieflyz.subscription_service.model.dto.response

data class SubscriptionResponse(
    val id: Long,
    val memberId: Long,
    val email: String,
    val country: String,
    val city: String,
    val plan: String,
    val payCount: Int,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)