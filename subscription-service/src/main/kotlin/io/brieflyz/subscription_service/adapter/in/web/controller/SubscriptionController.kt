package io.brieflyz.subscription_service.adapter.`in`.web.controller

import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.adapter.`in`.web.dto.mapper.toCommand
import io.brieflyz.subscription_service.adapter.`in`.web.dto.mapper.toResponse
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateSubscriptionRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.application.port.`in`.CancelSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.CreateSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionDetailsUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionListUseCase
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
    private val createSubscriptionUseCase: CreateSubscriptionUseCase,
    private val querySubscriptionDetailsUseCase: QuerySubscriptionDetailsUseCase,
    private val querySubscriptionListUseCase: QuerySubscriptionListUseCase,
    private val cancelSubscriptionUseCase: CancelSubscriptionUseCase,
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createSubscription(
        @JwtSubject username: String,
        @RequestBody @Valid request: CreateSubscriptionRequest
    ): ApiResponse<Long> {
        val subscriptionId = createSubscriptionUseCase.create(request.toCommand(username))
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_SUCCESS, subscriptionId)
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getMySubscriptionList(@JwtSubject username: String): ApiResponse<List<SubscriptionQueryResponse>> {
        val results = querySubscriptionListUseCase.queryListByMemberEmail(username)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, results.toResponse())
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionDetails(@PathVariable id: Long): ApiResponse<SubscriptionQueryResponse> {
        val result = querySubscriptionDetailsUseCase.queryBySubscriptionId(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, result.toResponse())
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun cancelSubscription(@PathVariable id: Long): ApiResponse<Long> {
        val canceledSubscriptionId = cancelSubscriptionUseCase.cancel(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_CANCEL_SUCCESS, canceledSubscriptionId)
    }
}