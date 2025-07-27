package io.brieflyz.subscription_service.controller

import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.subscription_service.service.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/subscriptions")
class SubscriptionAdminController(
    private val subscriptionService: SubscriptionService
) {
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteSubscription(@PathVariable id: Long): ApiResponse<Long> {
        subscriptionService.hardDeleteSubscriptionById(id)
        return ApiResponse.success(SuccessStatus.SUBSCRIBE_CANCEL_SUCCESS)
    }
}