package io.brieflyz.auth_service.application.dto

data class TokenResult(
    val accessToken: String,
    val accessTokenValidTime: Long
)