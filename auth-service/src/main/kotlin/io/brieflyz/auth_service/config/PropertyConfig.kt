package io.brieflyz.auth_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    fun authServiceProperties() = AuthServiceProperties()
}

@ConfigurationProperties(prefix = "app.auth")
@EnableConfigurationProperties(AuthServiceProperties::class)
data class AuthServiceProperties(
    var oauth: OAuthProperties? = null,
    var kafka: KafkaProperties? = null,
) {
    data class OAuthProperties(
        var authorizationUri: String = "",
        var redirectUri: String = "",
        var authorizedRedirectUris: List<String> = emptyList()
    )

    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )
}