package io.brieflyz.core.dto.kafka

import io.brieflyz.core.constants.DocumentType

sealed interface KafkaMessage

// TODO: chat-service에서 ai-service로 비동기 문서 생성 요청
data class DocumentRequestMessage(
    val title: String,
    val content: String,
    val documentType: DocumentType,
    val templateName: String,
    val sections: List<String>? = emptyList(),
    val additionalOptions: Map<String, Any>? = null
) : KafkaMessage