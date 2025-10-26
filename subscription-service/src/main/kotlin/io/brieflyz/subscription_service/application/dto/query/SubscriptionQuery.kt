package io.brieflyz.subscription_service.application.dto.query

import com.querydsl.core.types.Order

data class SubscriptionQuery(
    val page: Int,
    val size: Int,
    val isDeleted: Boolean?,
    val email: String?,
    val plan: String?,
    val paymentMethod: String?,
    val order: Order? = Order.DESC
)