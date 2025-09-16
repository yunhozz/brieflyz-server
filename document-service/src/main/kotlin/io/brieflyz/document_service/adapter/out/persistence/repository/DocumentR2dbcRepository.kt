package io.brieflyz.document_service.adapter.out.persistence.repository

import io.brieflyz.document_service.adapter.out.persistence.entity.DocumentEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import reactor.core.publisher.Flux

interface DocumentR2dbcRepository : R2dbcRepository<DocumentEntity, String> {
    fun findAllByUsernameOrderByUpdatedAtDesc(username: String): Flux<DocumentEntity>
}