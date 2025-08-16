package io.brieflyz.auth_service.model.dto.response

data class TokenResponse(
    val accessToken: String,
    val accessTokenValidTime: Long
)