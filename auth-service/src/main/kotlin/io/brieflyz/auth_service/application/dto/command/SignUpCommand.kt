package io.brieflyz.auth_service.application.dto.command

data class SignUpCommand(
    val email: String,
    val password: String,
    val nickname: String
)