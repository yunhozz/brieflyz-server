package io.brieflyz.subscription_service.model.dto.request

data class SubscriptionQueryRequest(
    val isDeleted: Boolean?,
    val memberId: Long?,
    val email: String?,
    val plan: String?,
    val paymentMethod: String?
)