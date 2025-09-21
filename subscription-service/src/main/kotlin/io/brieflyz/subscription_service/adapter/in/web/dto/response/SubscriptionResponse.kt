package io.brieflyz.subscription_service.adapter.`in`.web.dto.response

data class SubscriptionResponse(
    val id: Long,
    val email: String,
    val country: String,
    val city: String,
    val plan: String,
    val payCount: Int,
    val deleted: Boolean,
    val createdAt: String,
    val updatedAt: String
)