package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.common.annotation.JwtHeader
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
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
    fun createSubscription(
        @JwtHeader username: String,
        @RequestBody @Valid request: SubscriptionCreateRequest
    ): ApiResponse<Long> {
        println("username = ${username}")
        val subscriptionId = subscriptionService.createSubscription(username, request)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_SUCCESS, subscriptionId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionDetails(@PathVariable id: Long): ApiResponse<SubscriptionQueryResponse> {
        val subscription = subscriptionService.getSubscriptionDetailsById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, subscription)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelSubscription(@PathVariable id: Long): ApiResponse<Long> {
        val canceledSubscriptionId = subscriptionService.cancelSubscriptionById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_CANCEL_SUCCESS, canceledSubscriptionId)
    }
}