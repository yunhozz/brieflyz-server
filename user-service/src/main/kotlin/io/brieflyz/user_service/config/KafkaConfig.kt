package io.brieflyz.user_service.config

import io.brieflyz.core.config.UserServiceProperties
import io.brieflyz.core.constants.KafkaTopic
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin.NewTopics

@Configuration
class KafkaConfig(
    private val userServiceProperties: UserServiceProperties
) {
    @Bean
    fun userServiceTopics(): NewTopics {
        val np = userServiceProperties.kafka?.numOfPartitions!!
        val rf = userServiceProperties.kafka?.replicationFactor!!

        return NewTopics(
            NewTopic(KafkaTopic.LOGIN_RESPONSE_TOPIC, np, rf),
            NewTopic(KafkaTopic.AUTH_REQUEST_TOPIC, np, rf)
        )
    }
}