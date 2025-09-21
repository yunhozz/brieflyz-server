package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult

interface SubscriptionQueryPort {
    fun queryWithPaymentsById(subscriptionId: Long): SubscriptionQueryResult?
    fun queryListByMemberEmail(email: String): List<SubscriptionQueryResult>
    fun queryPageWithSubscriptionQuery(q: SubscriptionQuery): PageResult<SubscriptionQueryResult>
}