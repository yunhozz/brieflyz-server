package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionSimpleQueryResponse
import io.brieflyz.subscription_service.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSubscription(@RequestBody @Valid request: SubscriptionCreateRequest): ApiResponse<Long> {
        // TODO: member ID 입력
        val subscriptionId = subscriptionService.createSubscription(100L, request)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_SUCCESS, subscriptionId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionDetails(@PathVariable id: Long): ApiResponse<SubscriptionQueryResponse> {
        val subscription = subscriptionService.getSubscriptionDetailsById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, subscription)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionPage(
        @ModelAttribute request: SubscriptionQueryRequest,
        pageable: Pageable
    ): ApiResponse<List<SubscriptionSimpleQueryResponse>> {
        val subscriptionPage = subscriptionService.getSubscriptionPageByQuery(request, pageable)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, subscriptionPage)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelSubscription(@PathVariable id: Long): ApiResponse<Long> {
        val canceledSubscriptionId = subscriptionService.cancelSubscriptionById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_CANCEL_SUCCESS, canceledSubscriptionId)
    }
}