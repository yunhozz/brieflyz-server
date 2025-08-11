package io.brieflyz.ai_service.service.document

import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.ai_service.model.entity.Document
import reactor.core.publisher.Mono

interface DocumentManager {
    fun save(document: Document): Mono<DocumentResponse>
    fun updateStatus(documentId: String, fileName: String, fileUrl: String, downloadUrl: String): Mono<Void>
}