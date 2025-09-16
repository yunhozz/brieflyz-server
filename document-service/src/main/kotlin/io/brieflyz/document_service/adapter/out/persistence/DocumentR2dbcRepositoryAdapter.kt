package io.brieflyz.document_service.adapter.out.persistence

import io.brieflyz.document_service.adapter.out.persistence.entity.DocumentEntity
import io.brieflyz.document_service.adapter.out.persistence.repository.DocumentR2dbcRepository
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.domain.model.Document
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class DocumentR2dbcRepositoryAdapter(
    private val documentR2dbcRepository: DocumentR2dbcRepository
) : DocumentRepositoryPort {

    override fun save(document: Document): Mono<Document> =
        documentR2dbcRepository.save(document.toEntity())
            .map { it.toDomain() }

    override fun findByDocumentId(documentId: String): Mono<Document> =
        documentR2dbcRepository.findById(documentId)
            .map { it.toDomain() }

    override fun findAllByUsernameOrderByUpdatedAtDesc(username: String): Flux<Document> =
        documentR2dbcRepository.findAllByUsernameOrderByUpdatedAtDesc(username)
            .map { it.toDomain() }
}

private fun Document.toEntity() = DocumentEntity(
    documentId = this.documentId,
    username = this.username,
    title = this.title,
    type = this.type,
    status = this.status,
    fileName = this.fileName,
    fileUrl = this.fileUrl,
    downloadUrl = this.downloadUrl,
    errorMessage = this.errorMessage
)

private fun DocumentEntity.toDomain() = Document(
    documentId = this.documentId!!,
    username = this.username,
    title = this.title,
    type = this.type
)