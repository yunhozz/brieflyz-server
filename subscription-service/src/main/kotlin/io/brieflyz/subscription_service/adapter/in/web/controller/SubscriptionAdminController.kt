package io.brieflyz.subscription_service.adapter.`in`.web.controller

import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.adapter.`in`.web.dto.mapper.toResponse
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.QuerySubscriptionRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.port.`in`.DeleteSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionPageUseCase
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/subscriptions")
class SubscriptionAdminController(
    private val querySubscriptionPageUseCase: QuerySubscriptionPageUseCase,
    private val deleteSubscriptionUseCase: DeleteSubscriptionUseCase
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionPage(
        @ModelAttribute request: QuerySubscriptionRequest,
        pageable: Pageable
    ): ApiResponse<List<SubscriptionQueryResponse>> {
        val subscriptionQuery = SubscriptionQuery(
            pageable.offset.toInt(),
            pageable.pageSize,
            request.isDeleted,
            request.email,
            request.plan,
            request.paymentMethod,
            request.order
        )
        val pageResult = querySubscriptionPageUseCase.queryPageByQuery(subscriptionQuery)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, pageResult.toResponse())
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteSubscription(@PathVariable id: Long): ApiResponse<Void> {
        deleteSubscriptionUseCase.delete(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_DELETE_SUCCESS)
    }
}