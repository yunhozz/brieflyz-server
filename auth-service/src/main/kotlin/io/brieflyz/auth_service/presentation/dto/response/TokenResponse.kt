package io.brieflyz.auth_service.presentation.dto.response

data class TokenResponse(
    val accessToken: String,
    val accessTokenValidTime: Long
)