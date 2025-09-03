package io.brieflyz.auth_service.application.dto.command

data class SignInCommand(
    val email: String,
    val password: String
)