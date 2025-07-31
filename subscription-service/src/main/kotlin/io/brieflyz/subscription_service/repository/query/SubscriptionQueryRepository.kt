package io.brieflyz.subscription_service.repository.query

import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SubscriptionQueryRepository {
    fun findWithPaymentsByIdQuery(id: Long): SubscriptionQueryResponse?
    fun findListByMemberEmailQuery(email: String): List<SubscriptionQueryResponse>
    fun findPageWithPaymentsQuery(
        request: SubscriptionQueryRequest,
        pageable: Pageable
    ): Page<SubscriptionQueryResponse>
}