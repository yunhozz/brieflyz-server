package io.brieflyz.subscription_service.adapter.`in`.web.dto.validation

import jakarta.validation.ConstraintValidatorContext

interface Validatable {
    fun validate(ctx: ConstraintValidatorContext): Boolean
}