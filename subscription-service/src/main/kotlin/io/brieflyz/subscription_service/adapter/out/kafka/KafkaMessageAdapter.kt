package io.brieflyz.subscription_service.adapter.out.kafka

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.KafkaMessage
import io.brieflyz.core.dto.message.SubscriptionMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.application.port.out.MessagePort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaMessageAdapter(
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage>
) : MessagePort {

    private val log = logger()

    override fun sendSubscriptionMessage(message: Any) {
        require(message is SubscriptionMessage)
        kafkaTemplate.send(KafkaTopic.SUBSCRIPTION_TOPIC, message)
            .thenAccept { result ->
                val metadata = result.recordMetadata
                log.debug(
                    "[Kafka Send Success] topic={}, partition={}, offset={}, producerRecord={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    result.producerRecord
                )
            }.exceptionally { ex ->
                log.error("Unable to send message due to : ${ex.message}", ex)
                null
            }
    }
}