package io.brieflyz.auth_service.application.dto

data class SignUpRequestDto(
    val email: String,
    val password: String,
    val nickname: String
)