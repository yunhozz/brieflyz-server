package io.brieflyz.core.dto.kafka

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType

sealed interface KafkaMessage

data class DocumentStructureRequestMessage(
    val aiProvider: AiProvider,
    val title: String,
    val content: String,
    val documentType: DocumentType,
    val templateName: String?,
    val sections: List<String>?,
    val additionalOptions: Map<String, Any>?
) : KafkaMessage

data class DocumentStructureResponseMessage(
    val title: String,
    val documentType: DocumentType,
    val structure: Any
) : KafkaMessage