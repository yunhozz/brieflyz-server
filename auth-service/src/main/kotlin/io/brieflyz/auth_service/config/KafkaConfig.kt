package io.brieflyz.auth_service.config

import io.brieflyz.core.config.AuthServiceProperties
import io.brieflyz.core.constants.KafkaTopic
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin.NewTopics

@Configuration
class KafkaConfig(
    private val authServiceProperties: AuthServiceProperties
) {
    @Bean
    fun authServiceTopics(): NewTopics {
        val np = authServiceProperties.kafka?.numOfPartitions ?: 0
        val rf = authServiceProperties.kafka?.replicationFactor ?: 0

        return NewTopics(
            NewTopic(KafkaTopic.LOGIN_REQUEST_TOPIC, np, rf),
            NewTopic(KafkaTopic.AUTH_RESPONSE_TOPIC, np, rf)
        )
    }
}