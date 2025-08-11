package io.brieflyz.ai_service.service.document

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentGenerateRequest
import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.core.constants.DocumentType
import reactor.core.publisher.Mono

interface DocumentGenerator {
    fun getDocumentType(): DocumentType
    fun generateDocument(aiProvider: AiProvider, request: DocumentGenerateRequest): Mono<DocumentResponse>
}