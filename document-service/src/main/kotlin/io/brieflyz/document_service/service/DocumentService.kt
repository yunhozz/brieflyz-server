package io.brieflyz.document_service.service

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.DocumentStructureRequestMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.model.dto.DocumentCreateRequest
import io.brieflyz.document_service.model.dto.DocumentResourceResponse
import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.model.entity.Document
import io.brieflyz.document_service.repository.DocumentRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.net.URI
import java.nio.file.Paths
import java.util.UUID

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val kafkaSender: KafkaSender
) {
    private val log = logger()

    @Transactional
    fun createDocumentWithAi(request: DocumentCreateRequest): Mono<DocumentResponse> {
        val documentId = UUID.randomUUID().toString()
        val document = Document(documentId, request.title, request.documentType)

        return documentRepository.save(document)
            .flatMap { document ->
                val message = DocumentStructureRequestMessage(
                    request.aiProvider,
                    documentId,
                    title = document.title,
                    request.content,
                    documentType = document.type,
                    request.templateName,
                    request.sections,
                    request.additionalOptions
                )

                kafkaSender.sendReactive(KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC, message)
                    .then(Mono.just(document.toResponse()))
            }
    }

    @Transactional
    fun save(document: Document): Mono<Void> =
        documentRepository.save(document)
            .doOnSuccess { log.info("Document save success. ID=${document.id}, title=${document.title}") }
            .onErrorResume { ex ->
                log.error("Failed to save document. Title=${document.title}", ex)
                Mono.empty()
            }
            .then()

    @Transactional
    fun updateDocumentStatus(documentId: String, status: DocumentStatus, errorMessage: String?): Mono<Void> =
        findDocumentById(documentId)
            .flatMap { document ->
                document.updateStatus(status, errorMessage)
                documentRepository.save(document)
            }
            .then()

    @Transactional
    fun updateFileInfo(documentId: String, fileName: String, fileUrl: String, downloadUrl: String): Mono<Void> =
        findDocumentById(documentId)
            .flatMap { document ->
                document.updateForComplete(fileName, fileUrl, downloadUrl)
                log.debug("Document info={}", document.toString())
                documentRepository.save(document)
            }
            .doOnSuccess {
                log.info(
                    "Document update success. " +
                            "ID=${it.id}, " +
                            "title=${it.title}, " +
                            "file name=${it.fileName}, " +
                            "file URL=${it.fileUrl}"
                )
            }
            .onErrorResume { ex ->
                log.error("Failed to update document for ID=$documentId", ex)
                Mono.empty()
            }
            .then()

    @Transactional(readOnly = true)
    fun getDocumentInfo(documentId: String): Mono<DocumentResponse?> =
        findDocumentById(documentId).map { it.toResponse() }

    @Transactional(readOnly = true)
    fun createDocumentResource(documentId: String): Mono<DocumentResourceResponse> =
        findDocumentById(documentId)
            .flatMap { document ->
                if (document.status == DocumentStatus.COMPLETED) {
                    val mediaType = when (document.type) {
                        DocumentType.EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        DocumentType.POWERPOINT -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                    }
                    Mono.just(
                        DocumentResourceResponse(
                            fileName = document.fileName!!,
                            resource = FileSystemResource(Paths.get(URI(document.fileUrl!!))),
                            mediaType
                        )
                    )
                } else {
                    Mono.error(IllegalArgumentException("Document is not completed. ID=$documentId, status=${document.status}"))
                }
            }

    private fun findDocumentById(documentId: String): Mono<Document> =
        documentRepository.findById(documentId)
            .switchIfEmpty {
                Mono.error {
                    IllegalArgumentException("Document not found. ID=$documentId")
                }
            }

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