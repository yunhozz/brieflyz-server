package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionSimpleQueryResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface SubscriptionQueryRepository {
    fun findWithPaymentsByIdQuery(id: Long): SubscriptionQueryResponse?
    fun findPageWithPaymentsQuery(
        request: SubscriptionQueryRequest,
        pageable: Pageable
    ): Page<SubscriptionSimpleQueryResponse>
}