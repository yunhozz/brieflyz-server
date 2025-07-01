package io.brieflyz.auth_service.model.dto

data class TokenResponseDTO(
    val accessToken: String,
    val accessTokenValidTime: Long
)