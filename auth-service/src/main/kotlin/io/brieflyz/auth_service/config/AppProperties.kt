package io.brieflyz.auth_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties,
    val oauth: OAuthProperties?
) {
    data class JwtProperties(
        var tokenType: String?,
        var accessTokenValidTime: Long?,
        var refreshTokenValidTime: Long?
    )

    data class OAuthProperties(
        var authorizedRedirectUris: List<String>?
    )
}