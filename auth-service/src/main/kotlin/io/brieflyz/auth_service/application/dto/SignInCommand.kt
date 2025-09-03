package io.brieflyz.auth_service.application.dto

data class SignInCommand(
    val email: String,
    val password: String
)