package io.brieflyz.core.beans.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
@EnableConfigurationProperties(JwtProperties::class)
data class JwtProperties(
    val secretKey: String = "",
    val tokenType: String = "",
    val accessTokenValidTime: Long = 0,
    val refreshTokenValidTime: Long = 0
)