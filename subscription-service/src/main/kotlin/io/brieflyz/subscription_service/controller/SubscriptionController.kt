package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.model.dto.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.SubscriptionUpdateRequest
import io.brieflyz.subscription_service.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        val subscriptionId = subscriptionService.createSubscription(request)
        return ApiResponse.success(SuccessCode.SUBSCRIBE_SUCCESS, subscriptionId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscription(@PathVariable id: Long): ApiResponse<SubscriptionResponse> {
        val subscription = subscriptionService.getSubscription(id)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, subscription)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionsByParams(
        @RequestParam(required = false) memberId: Long?,
        @RequestParam(required = false) email: String?
    ): ApiResponse<List<SubscriptionResponse>> {
        val subscriptions = subscriptionService.getSubscriptionsByMemberIdOrEmail(memberId, email)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, subscriptions)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateSubscription(
        @PathVariable id: Long,
        @RequestBody @Valid request: SubscriptionUpdateRequest
    ): ApiResponse<Long> {
        val subscriptionId = subscriptionService.updateSubscription(id, request)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_UPDATE_SUCCESS, subscriptionId)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelSubscription(@PathVariable id: Long): ApiResponse<Void> {
        subscriptionService.deleteSubscription(id)
        return ApiResponse.success(SuccessCode.SUBSCRIBE_CANCEL_SUCCESS)
    }
}