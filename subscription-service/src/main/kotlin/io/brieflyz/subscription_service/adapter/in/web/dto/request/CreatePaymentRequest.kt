package io.brieflyz.subscription_service.adapter.`in`.web.dto.request

import io.brieflyz.subscription_service.adapter.`in`.web.dto.validation.CreatePaymentRequestValidator
import io.brieflyz.subscription_service.adapter.`in`.web.dto.validation.CustomValidate
import io.brieflyz.subscription_service.adapter.`in`.web.dto.validation.Validatable
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@CustomValidate
data class CreatePaymentRequest(
    @field:NotNull(message = "결제 금액은 필수입니다.")
    val charge: Double,

    @field:NotBlank(message = "결제 방식은 필수입니다.")
    val method: String,

    val details: CreatePaymentDetailsRequest
) : Validatable {

    override fun validate(ctx: ConstraintValidatorContext): Boolean =
        CreatePaymentRequestValidator.validate(method, details, ctx)
}