package io.brieflyz.auth_service.application.dto

data class SignInRequestDto(
    val email: String,
    val password: String
)