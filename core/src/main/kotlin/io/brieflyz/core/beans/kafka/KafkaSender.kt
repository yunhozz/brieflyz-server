package io.brieflyz.core.beans.kafka

import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component

@Component
class KafkaSender(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>,
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, KafkaMessage>
) {
    private val log = logger()

    fun send(topic: String, message: KafkaMessage) {
        kafkaTemplate.send(topic, message)
            .thenAccept { result ->
                val metadata = result.recordMetadata
                log.debug(
                    """
                    [Kafka Send Success]
                    Topic: ${metadata.topic()}
                    Partition: ${metadata.partition()}
                    Offset: ${metadata.offset()}
                    Producer Record: ${result.producerRecord}
                """.trimIndent()
                )
            }.exceptionally { ex ->
                log.error("Unable to send message due to : ${ex.message}", ex)
                null
            }
    }

    fun sendReactive(topic: String, message: KafkaMessage) {
        reactiveKafkaProducerTemplate.send(topic, message)
            .doOnNext { result ->
                val metadata = result.recordMetadata()
                log.debug(
                    """
                    [Kafka Send Success]
                    Topic: ${metadata.topic()}
                    Partition: ${metadata.partition()}
                    Offset: ${metadata.offset()}
                """.trimIndent()
                )
            }
            .doOnError { ex ->
                log.error("Unable to send message due to : ${ex.message}", ex)
            }
            .subscribe()
    }
}