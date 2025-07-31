package io.brieflyz.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
@EnableConfigurationProperties(AuthServiceProperties::class)
data class AuthServiceProperties(
    val oauth: OAuthProperties? = null,
    val kafka: KafkaProperties? = null,
) {
    data class OAuthProperties(
        val authorizationUri: String = "",
        val redirectUri: String = "",
        val authorizedRedirectUris: List<String> = emptyList()
    )

    data class KafkaProperties(
        val numOfPartitions: Int = 0,
        val replicationFactor: Short = 0
    )
}

@ConfigurationProperties(prefix = "app.subscription")
@EnableConfigurationProperties(SubscriptionServiceProperties::class)
data class SubscriptionServiceProperties(
    val kafka: KafkaProperties? = null
) {
    data class KafkaProperties(
        val numOfPartitions: Int = 0,
        val replicationFactor: Short = 0
    )
}

@ConfigurationProperties(prefix = "jwt")
@EnableConfigurationProperties(JwtProperties::class)
data class JwtProperties(
    val secretKey: String = "",
    val tokenType: String = "",
    val accessTokenValidTime: Long = 0,
    val refreshTokenValidTime: Long = 0
)