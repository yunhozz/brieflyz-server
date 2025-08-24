package io.brieflyz.document_service.service.impl

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.model.entity.Document
import io.brieflyz.document_service.repository.DocumentRepository
import io.brieflyz.document_service.service.DocumentManager
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Component
class DocumentManagerImpl(
    private val documentRepository: DocumentRepository,
    private val kafkaSender: KafkaSender
) : DocumentManager {

    // TODO: Kafka를 통해 AI Service로 문서 생성 결과 전송

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