package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.core.dto.kafka.KafkaMessage

interface MessagePort {
    fun send(topic: String, message: KafkaMessage)
}