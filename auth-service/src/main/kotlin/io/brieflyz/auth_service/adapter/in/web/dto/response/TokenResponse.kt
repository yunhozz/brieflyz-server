package io.brieflyz.auth_service.adapter.`in`.web.dto.response

data class TokenResponse(
    val accessToken: String,
    val accessTokenValidTime: Long
)