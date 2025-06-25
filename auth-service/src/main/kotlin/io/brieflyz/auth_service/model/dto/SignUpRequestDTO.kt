package io.brieflyz.auth_service.model.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class SignUpRequestDTO(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    val password: String
)