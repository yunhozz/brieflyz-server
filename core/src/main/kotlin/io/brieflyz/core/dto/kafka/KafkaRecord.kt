package io.brieflyz.core.dto.kafka

import io.brieflyz.core.constants.KafkaTopic

data class KafkaRecord(
    val topic: KafkaTopic,
    val message: KafkaMessage
)