package io.brieflyz.core.beans.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JwtConfig {
    @Bean
    @ConfigurationProperties(prefix = "jwt")
    fun jwtProperties() = JwtProperties()
}

data class JwtProperties(
    var secretKey: String = "",
    var tokenType: String = "",
    var accessTokenValidTime: Long = 0,
    var refreshTokenValidTime: Long = 0
)