package io.brieflyz.document_service.service.support

import io.brieflyz.core.constants.DocumentType
import reactor.core.publisher.Mono

interface DocumentGenerator {
    fun getDocumentType(): DocumentType
    fun generateDocument(documentId: String, title: String, structure: Any): Mono<Void>
}