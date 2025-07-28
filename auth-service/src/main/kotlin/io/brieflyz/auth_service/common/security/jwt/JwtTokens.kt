package io.brieflyz.auth_service.common.security.jwt

data class JwtTokens(
    val tokenType: String,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenValidTime: Long,
    val refreshTokenValidTime: Long
)