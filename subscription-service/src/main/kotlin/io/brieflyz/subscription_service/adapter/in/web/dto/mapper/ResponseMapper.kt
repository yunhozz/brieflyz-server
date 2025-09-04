package io.brieflyz.subscription_service.adapter.`in`.web.dto.mapper

import io.brieflyz.subscription_service.adapter.`in`.web.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult

fun List<SubscriptionQueryResult>.toResponse() = this.map {
    SubscriptionQueryResponse(it.id, it.email, it.plan, it.payCount, it.updatedAt)
}

fun SubscriptionQueryResult.toResponse() = SubscriptionQueryResponse(
    id,
    email,
    country,
    city,
    plan,
    payCount,
    createdAt,
    updatedAt
)

fun PageResult<SubscriptionQueryResult>.toResponse() = this.content.map {
    SubscriptionQueryResponse(it.id, it.email, it.country, it.city, it.plan, it.payCount, it.createdAt, it.updatedAt)
}