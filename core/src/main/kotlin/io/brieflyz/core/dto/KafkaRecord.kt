package io.brieflyz.core.dto

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.KafkaMessage

data class KafkaRecord(
    val topic: KafkaTopic,
    val message: KafkaMessage
)