package io.brieflyz.subscription_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.subscription")
    fun subscriptionServiceProperties() = SubscriptionServiceProperties()
}

data class SubscriptionServiceProperties(
    var kafka: KafkaProperties = KafkaProperties(),
    var email: EmailProperties = EmailProperties()
) {
    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class EmailProperties(
        var renewUrl: String = "",
    )
}