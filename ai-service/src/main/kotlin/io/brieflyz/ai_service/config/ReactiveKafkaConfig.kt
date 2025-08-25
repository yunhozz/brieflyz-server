package io.brieflyz.ai_service.config

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.KafkaMessage
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class ReactiveKafkaConfig {

    @Bean
    fun reactiveKafkaConsumerTemplate(
        @Qualifier("kafkaConsumerProperties") props: Map<String, Any>
    ): ReactiveKafkaConsumerTemplate<String, KafkaMessage> = ReactiveKafkaConsumerTemplate(
        ReceiverOptions.create<String, KafkaMessage>(props)
            .subscription(
                listOf(
                    KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC
                )
            )
    )
}