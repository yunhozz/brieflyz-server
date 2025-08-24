package io.brieflyz.document_service.service

import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.model.entity.Document
import reactor.core.publisher.Mono

interface DocumentManager {
    fun save(document: Document): Mono<DocumentResponse>
    fun updateStatus(documentId: String, fileName: String, fileUrl: String, downloadUrl: String): Mono<Void>
}