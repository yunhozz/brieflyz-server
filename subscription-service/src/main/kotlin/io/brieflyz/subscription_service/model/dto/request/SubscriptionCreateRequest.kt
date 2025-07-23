package io.brieflyz.subscription_service.model.dto.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SubscriptionCreateRequest(
    @field:NotBlank(message = "회원 이메일은 필수입니다.")
    @field:Email(message = "올바른 이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "국적은 필수입니다.")
    val country: String,

    @field:NotBlank(message = "도시 이름은 필수입니다.")
    val city: String,

    @field:NotBlank(message = "구독 플랜은 필수입니다.")
    val plan: String,

    @field:Valid
    val payment: PaymentCreateRequest
)