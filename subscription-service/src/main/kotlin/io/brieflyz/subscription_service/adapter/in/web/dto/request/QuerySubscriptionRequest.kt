package io.brieflyz.subscription_service.adapter.`in`.web.dto.request

import com.querydsl.core.types.Order

data class QuerySubscriptionRequest(
    val isDeleted: Boolean?,
    val email: String?,
    val plan: String?,
    val paymentMethod: String?,
    val order: Order? = Order.DESC
)