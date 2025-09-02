package io.brieflyz.auth_service.infrastructure.config

import io.brieflyz.auth_service.common.props.AuthServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin.NewTopics

@Configuration
class KafkaTopicConfig(
    private val authServiceProperties: AuthServiceProperties
) {
    @Bean
    fun authServiceTopics(): NewTopics {
        val np = authServiceProperties.kafka.numOfPartitions
        val rf = authServiceProperties.kafka.replicationFactor

        return NewTopics()
    }
}