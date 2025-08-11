package io.brieflyz.ai_service.service.document

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.kafka.DocumentRequestMessage
import reactor.core.publisher.Mono

interface DocumentGenerator {
    fun getDocumentType(): DocumentType
    fun generateDocument(aiProvider: AiProvider, request: DocumentRequestMessage): Mono<DocumentResponse>
}