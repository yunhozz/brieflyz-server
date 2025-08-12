package io.brieflyz.ai_service.service.document.impl

import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.ai_service.model.entity.Document
import io.brieflyz.ai_service.repository.DocumentRepository
import io.brieflyz.ai_service.service.document.DocumentManager
import io.brieflyz.core.utils.logger
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class DocumentManagerImpl(
    private val documentRepository: DocumentRepository
) : DocumentManager {

    private val log = logger()

    @Transactional
    override fun save(document: Document): Mono<DocumentResponse> =
        documentRepository.save(document)
            .doOnSuccess { log.info("Document save success. ID: ${document.id}, title: ${document.title}") }
            .onErrorResume { ex ->
                log.error("Failed to save document: ${ex.message}", ex)
                Mono.empty()
            }
            .map { document -> document.toResponse() }

    @Transactional
    override fun updateStatus(documentId: String, fileName: String, fileUrl: String, downloadUrl: String): Mono<Void> =
        documentRepository.findById(documentId)
            .flatMap { document ->
                document.updateForComplete(fileName, fileUrl, downloadUrl)
                log.debug(document.toString())
                documentRepository.save(document)
            }
            .doOnSuccess {
                log.info(
                    "Document update success. " +
                            "ID: ${it.id}, " +
                            "title: ${it.title}, " +
                            "file name: ${it.fileName}, " +
                            "URL: ${it.fileUrl}"
                )
            }
            .onErrorResume { ex ->
                log.error("Failed to update document: ${ex.message}", ex)
                Mono.empty()
            }
            .then()

    private fun Document.toResponse() = DocumentResponse(
        documentId = this.documentId,
        title = this.title,
        fileName = this.fileName,
        fileUrl = this.fileUrl,
        downloadUrl = this.downloadUrl,
        status = this.status,
        errorMessage = this.errorMessage,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}