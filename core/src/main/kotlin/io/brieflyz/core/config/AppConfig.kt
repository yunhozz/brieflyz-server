package io.brieflyz.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(
    GatewayProperties::class,
    AuthServiceProperties::class,
    SubscriptionServiceProperties::class
)
class AppConfig {
    @Bean
    fun gatewayProperties() = GatewayProperties()

    @Bean
    fun authServiceProperties() = AuthServiceProperties()

    @Bean
    fun subscriptionServiceProperties() = SubscriptionServiceProperties()
}

@ConfigurationProperties(prefix = "app.gateway")
data class GatewayProperties(
    val jwt: JwtProperties? = null,
) {
    data class JwtProperties(
        val secretKey: String = "",
        val tokenType: String = ""
    )
}

@ConfigurationProperties(prefix = "app.auth")
data class AuthServiceProperties(
    val jwt: JwtProperties? = null,
    val oauth: OAuthProperties? = null,
    val kafka: KafkaProperties? = null,
) {
    data class JwtProperties(
        val secretKey: String = "",
        val tokenType: String = "",
        val accessTokenValidTime: Long = 0L,
        val refreshTokenValidTime: Long = 0L
    )

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
data class SubscriptionServiceProperties(
    val kafka: KafkaProperties? = null
) {
    data class KafkaProperties(
        val numOfPartitions: Int = 0,
        val replicationFactor: Short = 0
    )
}