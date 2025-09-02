package io.brieflyz.auth_service.infrastructure.config

import io.brieflyz.auth_service.common.props.AuthServiceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.auth")
    fun authServiceProperties() = AuthServiceProperties()
}