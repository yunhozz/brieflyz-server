package io.brieflyz.core.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Bean
    fun authServiceProperties() = AuthServiceProperties()

    @Bean
    fun userServiceProperties() = UserServiceProperties()
}

@ConfigurationProperties(prefix = "app.auth")
@EnableConfigurationProperties(AuthServiceProperties::class)
data class AuthServiceProperties(
    val jwt: JwtProperties? = null,
    val oauth: OAuthProperties? = null,
    val kafka: KafkaProperties? = null,
) {
    data class JwtProperties(
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

@ConfigurationProperties(prefix = "app.user")
@EnableConfigurationProperties(UserServiceProperties::class)
data class UserServiceProperties(
    val kafka: KafkaProperties? = null
) {
    data class KafkaProperties(
        val numOfPartitions: Int = 0,
        val replicationFactor: Short = 0
    )
}