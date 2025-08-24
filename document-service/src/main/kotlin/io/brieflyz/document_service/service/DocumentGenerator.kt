package io.brieflyz.document_service.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.model.dto.DocumentResponse
import reactor.core.publisher.Mono

interface DocumentGenerator {
    fun getDocumentType(): DocumentType
    fun generateDocument(title: String, structure: Any): Mono<DocumentResponse>
}