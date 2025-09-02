package io.brieflyz.auth_service.application.dto

data class TokenResponseDto(
    val accessToken: String,
    val accessTokenValidTime: Long
)