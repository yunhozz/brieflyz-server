package io.brieflyz.core.dto.kafka

import io.brieflyz.core.constants.DocumentType

sealed interface KafkaMessage

data class DocumentCreateRequestMessage(
    val title: String,
    val documentType: DocumentType,
    val structure: Any
) : KafkaMessage