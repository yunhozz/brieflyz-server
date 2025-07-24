package io.brieflyz.subscription_service.model.dto.validation

import jakarta.validation.ConstraintValidatorContext

interface Validatable {
    fun validate(ctx: ConstraintValidatorContext): Boolean
}