package io.brieflyz.subscription_service.adapter.`in`.web.dto.validation

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CustomValidator : ConstraintValidator<CustomValidate, Validatable> {

    override fun isValid(v: Validatable, ctx: ConstraintValidatorContext): Boolean {
        ctx.disableDefaultConstraintViolation()
        return v.validate(ctx)
    }
}