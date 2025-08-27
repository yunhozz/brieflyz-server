package io.brieflyz.document_service.repository

import io.brieflyz.document_service.model.entity.Document
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface DocumentRepository : R2dbcRepository<Document, String> {
    fun findAllByUsernameOrderByUpdatedAtDesc(username: String): Flux<Document>
}