package io.brieflyz.auth_service.adapter.`in`.kafka

import io.brieflyz.auth_service.application.port.`in`.UpdateSubscriptionStatusUseCase
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.KafkaMessage
import io.brieflyz.core.dto.message.SubscriptionMessage
import io.brieflyz.core.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class SubscriptionStatusKafkaListener(
    private val updateSubscriptionStatusUseCase: UpdateSubscriptionStatusUseCase
) {
    private val log = logger()

    @KafkaListener(topics = [KafkaTopic.SUBSCRIPTION_TOPIC])
    fun updateBySubscriptionStatus(
        @Header(KafkaHeaders.RECEIVED_KEY, required = false) key: String?,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET, required = false) offset: Long?,
        @Header(KafkaHeaders.RECEIVED_TIMESTAMP) timestamp: Long,
        @Payload message: KafkaMessage,
        ack: Acknowledgment
    ) {
        log.debug(
            "[Kafka Received] key={}, topic={}, partition={}, offset={}, timestamp={}, message={}",
            key,
            topic,
            partition,
            offset,
            timestamp,
            message
        )
        require(message is SubscriptionMessage)
        updateSubscriptionStatusUseCase.updateBySubscriptionStatus(message.email, message.isCreated)
        ack.acknowledge()
    }
}