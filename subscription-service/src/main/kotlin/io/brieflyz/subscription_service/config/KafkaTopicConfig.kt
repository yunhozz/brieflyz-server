package io.brieflyz.subscription_service.config

import io.brieflyz.subscription_service.common.props.SubscriptionServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin.NewTopics

@Configuration
class KafkaTopicConfig(
    private val subscriptionServiceProperties: SubscriptionServiceProperties
) {
    @Bean
    fun subscriptionServiceTopics(): NewTopics {
        val np = subscriptionServiceProperties.kafka.numOfPartitions
        val rf = subscriptionServiceProperties.kafka.replicationFactor

        return NewTopics()
    }
}