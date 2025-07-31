package io.brieflyz.subscription_service.model.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank

data class SubscriptionCreateRequest(
    @field:NotBlank(message = "국적은 필수입니다.")
    val country: String,

    @field:NotBlank(message = "도시 이름은 필수입니다.")
    val city: String,

    @field:NotBlank(message = "구독 플랜은 필수입니다.")
    val plan: String,

    @field:Valid
    val payment: PaymentCreateRequest
)