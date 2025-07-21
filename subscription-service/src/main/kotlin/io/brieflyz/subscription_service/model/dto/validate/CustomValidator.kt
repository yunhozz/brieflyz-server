package io.brieflyz.subscription_service.model.dto.validate

import io.brieflyz.subscription_service.common.annotation.CustomValidate
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class CustomValidator : ConstraintValidator<CustomValidate, Validatable> {

    override fun isValid(v: Validatable, ctx: ConstraintValidatorContext): Boolean {
        ctx.disableDefaultConstraintViolation()
        return v.validate(ctx)
    }
}