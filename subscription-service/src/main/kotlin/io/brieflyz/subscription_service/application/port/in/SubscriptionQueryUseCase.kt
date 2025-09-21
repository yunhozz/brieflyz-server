package io.brieflyz.subscription_service.application.port.`in`

import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult

interface QuerySubscriptionDetailsUseCase {
    fun queryBySubscriptionId(subscriptionId: Long): SubscriptionQueryResult
}

interface QuerySubscriptionListUseCase {
    fun queryListByMemberEmail(email: String): List<SubscriptionQueryResult>
}

interface QuerySubscriptionPageUseCase {
    fun queryPageByQuery(query: SubscriptionQuery): PageResult<SubscriptionQueryResult>
}