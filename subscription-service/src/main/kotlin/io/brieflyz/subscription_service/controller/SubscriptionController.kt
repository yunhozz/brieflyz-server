package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.model.dto.CreateSubscriptionRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.UpdateSubscriptionRequest
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
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(
    private val subscriptionService: SubscriptionService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSubscription(@RequestBody @Valid request: CreateSubscriptionRequest): ApiResponse<Long> {
        val subscriptionId = subscriptionService.createSubscription(request)
        return ApiResponse.success(SuccessCode.SUBSCRIBE_SUCCESS, subscriptionId)
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscription(@PathVariable id: Long): ApiResponse<SubscriptionResponse> {
        val response = subscriptionService.getSubscription(id)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, response)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllSubscriptions(): ApiResponse<List<SubscriptionResponse>> {
        val responses = subscriptionService.getAllSubscriptions()
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, responses)
    }

    data class SubscriptionParams(
        val memberId: Long?,
        val email: String?
    )

    @GetMapping("/member/{memberId}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionsByMemberId(@PathVariable memberId: Long): ApiResponse<List<SubscriptionResponse>> {
        val responses = subscriptionService.getSubscriptionsByMemberId(memberId)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, responses)
    }

    @GetMapping("/email/{memberEmail}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionsByMemberEmail(@PathVariable memberEmail: String): ApiResponse<List<SubscriptionResponse>> {
        val responses = subscriptionService.getSubscriptionsByMemberEmail(memberEmail)
        return ApiResponse.success(SuccessCode.SUBSCRIPTION_INFO_READ_SUCCESS, responses)
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    fun updateSubscription(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateSubscriptionRequest
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