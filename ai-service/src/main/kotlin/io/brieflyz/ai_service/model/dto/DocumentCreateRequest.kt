package io.brieflyz.ai_service.model.dto

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.core.constants.DocumentType

data class DocumentCreateRequest(
    val aiProvider: AiProvider,
    val title: String,
    val content: String,
    val documentType: DocumentType,
    val templateName: String,
    val sections: List<String>? = emptyList(),
    val additionalOptions: Map<String, Any>? = null
)