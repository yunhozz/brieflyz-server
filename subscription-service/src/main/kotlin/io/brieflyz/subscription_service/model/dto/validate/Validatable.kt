package io.brieflyz.subscription_service.model.dto.validate

import jakarta.validation.ConstraintValidatorContext

interface Validatable {
    fun validate(ctx: ConstraintValidatorContext): Boolean
}