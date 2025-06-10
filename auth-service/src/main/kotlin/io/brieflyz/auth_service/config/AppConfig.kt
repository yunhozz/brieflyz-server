package io.brieflyz.auth_service.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppConfig(
    val jwt: JwtProperties,
    val oauth: OAuthProperties,
    val kafka: KafkaProperties
) {
    data class JwtProperties(
        val tokenType: String = "",
        val accessTokenValidTime: Long = 0L,
        val refreshTokenValidTime: Long = 0L
    )

    data class OAuthProperties(
        val authorizedRedirectUris: List<String> = emptyList()
    )

    data class KafkaProperties(
        val numOfPartitions: Int = 0,
        val replicationFactor: Short = 0
    )
}