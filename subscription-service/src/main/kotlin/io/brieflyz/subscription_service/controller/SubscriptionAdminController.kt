package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.service.SubscriptionService
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
    private val subscriptionService: SubscriptionService
) {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getSubscriptionPage(
        @ModelAttribute request: SubscriptionQueryRequest,
        pageable: Pageable
    ): ApiResponse<List<SubscriptionQueryResponse>> {
        val subscriptionPage = subscriptionService.getSubscriptionPageByQuery(request, pageable)
        return ApiResponse.success(SuccessStatus.SUBSCRIPTION_INFO_READ_SUCCESS, subscriptionPage)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteSubscription(@PathVariable id: Long): ApiResponse<Void> {
        subscriptionService.hardDeleteSubscriptionById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_DELETE_SUCCESS)
    }
}