package io.brieflyz.subscription_service.adapter.`in`.web.dto.request

import jakarta.validation.constraints.NotBlank

data class UpdateSubscriptionRequest(
    @field:NotBlank(message = "구독 플랜은 필수입니다.")
    val plan: String
)