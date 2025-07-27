package io.brieflyz.subscription_service.model.dto.request

import com.querydsl.core.types.Order

data class SubscriptionQueryRequest(
    val isDeleted: Boolean?,
    val memberId: Long?,
    val email: String?,
    val plan: String?,
    val paymentMethod: String?,
    val order: Order? = Order.DESC
)