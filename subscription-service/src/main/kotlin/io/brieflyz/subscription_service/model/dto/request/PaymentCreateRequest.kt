package io.brieflyz.subscription_service.model.dto.request

import io.brieflyz.subscription_service.common.annotation.CustomValidate
import io.brieflyz.subscription_service.model.dto.validate.PaymentCreateRequestValidator
import io.brieflyz.subscription_service.model.dto.validate.Validatable
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@CustomValidate
data class PaymentCreateRequest(
    @field:NotNull(message = "결제 금액은 필수입니다.")
    val charge: Double,

    @field:NotBlank(message = "결제 방식은 필수입니다.")
    val method: String,

    val details: PaymentDetailsCreateRequest
) : Validatable {

    override fun validate(ctx: ConstraintValidatorContext): Boolean =
        PaymentCreateRequestValidator.validate(method, details, ctx)
}