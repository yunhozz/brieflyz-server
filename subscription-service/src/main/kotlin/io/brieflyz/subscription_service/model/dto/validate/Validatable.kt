package io.brieflyz.subscription_service.model.dto.validate

import jakarta.validation.ConstraintValidatorContext

interface Validatable {
    fun validate(ctx: ConstraintValidatorContext): Boolean

    fun ConstraintValidatorContext.addViolation(message: String, property: String): Boolean {
        this.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation()
        return false
    }
}