package io.brieflyz.auth_service.application.dto.result

data class TokenResult(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenValidTime: Long,
    val refreshTokenValidTime: Long
)