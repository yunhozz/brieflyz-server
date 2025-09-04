package io.brieflyz.subscription_service.adapter.out.kafka

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.subscription_service.application.port.out.MessagePort
import org.springframework.stereotype.Component

@Component
class KafkaAdapter(
    private val kafkaSender: KafkaSender
) : MessagePort {

    override fun send(topic: String, message: KafkaMessage) {
        kafkaSender.send(topic, message)
    }
}