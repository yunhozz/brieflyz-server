package io.brieflyz.auth_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.auth")
    fun authServiceProperties() = AuthServiceProperties()
}

data class AuthServiceProperties(
    var oauth: OAuthProperties = OAuthProperties(),
    var kafka: KafkaProperties = KafkaProperties(),
    var email: EmailProperties = EmailProperties()
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

    data class EmailProperties(
        var verifyUrl: String = ""
    )
}