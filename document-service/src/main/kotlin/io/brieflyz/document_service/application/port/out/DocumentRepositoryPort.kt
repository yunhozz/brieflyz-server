package io.brieflyz.document_service.application.port.out

import io.brieflyz.document_service.domain.model.Document
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface DocumentRepositoryPort {
    fun save(document: Document): Mono<Document>
    fun findByDocumentId(documentId: String): Mono<Document>
    fun findAllByUsernameOrderByUpdatedAtDesc(username: String): Flux<Document>
    fun updateStatus(updatedDocument: Document): Mono<Void>
    fun updateFileInfo(updatedDocument: Document): Mono<Void>
}