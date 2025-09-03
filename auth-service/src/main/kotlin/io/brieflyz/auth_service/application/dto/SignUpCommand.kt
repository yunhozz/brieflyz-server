package io.brieflyz.auth_service.application.dto

data class SignUpCommand(
    val email: String,
    val password: String,
    val nickname: String
)