package io.brieflyz.subscription_service.adapter.`in`.web.dto.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [CustomValidator::class])
annotation class CustomValidate(
    val message: String = "Invalid Request",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)