package io.brieflyz.subscription_service.model.dto.request

import jakarta.validation.constraints.NotBlank

data class SubscriptionUpdateRequest(
    @field:NotBlank(message = "구독 플랜은 필수입니다.")
    val plan: String
)