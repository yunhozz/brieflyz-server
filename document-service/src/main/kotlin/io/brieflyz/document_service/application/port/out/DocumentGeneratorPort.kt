package io.brieflyz.document_service.application.port.out

import io.brieflyz.core.constants.DocumentType
import reactor.core.publisher.Mono

interface DocumentGeneratorPort {
    fun getDocumentType(): DocumentType
    fun generateDocument(documentId: String, title: String, structure: Any?): Mono<Void>
    fun updateDocumentFailed(documentId: String, errMsg: String): Mono<Void>
}