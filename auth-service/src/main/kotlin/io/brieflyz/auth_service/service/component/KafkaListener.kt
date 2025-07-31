package io.brieflyz.auth_service.service.component

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class KafkaListener {

    private val log = logger()

    @KafkaListener(topics = [KafkaTopic.DEFAULT_TOPIC])
    fun listen(
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.OFFSET, required = false) offset: Long?,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Payload message: KafkaMessage,
        ack: Acknowledgment
    ) {
        log.debug(
            """
            [Kafka Received]
            Key: $key
            Offset: $offset
            Partition: $partition
            Topic: $topic
            Timestamp: $timestamp
            Message: $message
        """.trimIndent()
        )

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
        log.error(
            """
            [Dead Letter Consumed]
            Topic: $topic
            Partition: $partition
            Offset: $offset
            Headers: $headers
            Payload: $payload
        """.trimIndent()
        )
    }
}