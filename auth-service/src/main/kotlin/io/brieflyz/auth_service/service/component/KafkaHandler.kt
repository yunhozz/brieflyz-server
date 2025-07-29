package io.brieflyz.auth_service.service.component

import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class KafkaHandler(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>
) {
    private val log = logger()

    fun send(topic: String, message: KafkaMessage) {
        try {
            val result = kafkaTemplate.send(topic, message).get(10, TimeUnit.SECONDS)
            val metadata = result.recordMetadata

            log.debug("Topic: ${metadata.topic()}")
            log.debug("Partition: ${metadata.partition()}")
            log.debug("Offset: ${metadata.offset()}")
            log.debug("Producer Record: {}", result.producerRecord)

        } catch (e: Exception) {
            log.error("Kafka Send Fail: ${e.message}")
        }
    }

    //    @KafkaListener(topics = [KafkaTopic.LOGIN_RESPONSE_TOPIC, KafkaTopic.AUTH_REQUEST_TOPIC])
    fun listen(
        @Header(KafkaHeaders.OFFSET, required = false) offset: Long?,
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) ts: Long,
        @Payload message: KafkaMessage,
        ack: Acknowledgment
    ) {
        log.debug("Kafka Offset: $offset")
        log.debug("Kafka Key: $key")
        log.debug("Kafka Partition: $partition")
        log.debug("Kafka Topic: $topic")
        log.debug("Kafka Timestamp: $ts")
        log.debug("Kafka Message Body: {}", message)

        ack.acknowledge()
    }

    @KafkaListener(topicPattern = "\\*-dlt")
    fun handleDeadLetter(
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int?,
        @Header(KafkaHeaders.OFFSET) offset: Long?,
        @Headers headers: MessageHeaders,
        @Payload payload: Any?
    ) {
        log.error("DLT consumed - topic: $topic, partition: $partition, offset: $offset")
        log.error("DLT payload: {}", payload)
        log.error("DLT headers: {}", headers)
    }
}