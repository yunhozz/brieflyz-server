package io.brieflyz.core.dto.kafka

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType

sealed interface KafkaMessage

data class SubscriptionMessage(
    val email: String,
    val isCreated: Boolean
) : KafkaMessage

data class DocumentStructureRequestMessage(
    val aiProvider: AiProvider,
    val documentId: String,
    val title: String,
    val content: String,
    val documentType: DocumentType,
    val templateName: String?,
    val sections: List<String>?,
    val additionalOptions: Map<String, Any>?
) : KafkaMessage

data class DocumentStructureResponseMessage(
    val documentId: String,
    val title: String,
    val documentType: DocumentType,
    val structure: Any?,
    val errMsg: String?
) : KafkaMessage