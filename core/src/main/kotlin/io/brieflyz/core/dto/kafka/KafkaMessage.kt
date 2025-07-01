package io.brieflyz.core.dto.kafka

interface KafkaMessage

data class TestMessage(
    val data: Map<String, Any>
) : KafkaMessage