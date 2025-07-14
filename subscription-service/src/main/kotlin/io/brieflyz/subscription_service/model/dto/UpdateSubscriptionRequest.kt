package io.brieflyz.subscription_service.model.dto

import jakarta.validation.constraints.NotBlank

data class UpdateSubscriptionRequest(
    @field:NotBlank(message = "구독 간격은 필수입니다.")
    val subscriptionInterval: String
)