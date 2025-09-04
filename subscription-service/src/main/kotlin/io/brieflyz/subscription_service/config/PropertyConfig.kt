package io.brieflyz.subscription_service.config

import io.brieflyz.subscription_service.common.props.SubscriptionServiceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.subscription")
    fun subscriptionServiceProperties() = SubscriptionServiceProperties()
}