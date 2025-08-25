package io.brieflyz.document_service.service

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.DocumentStructureRequestMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.model.dto.DocumentCreateRequest
import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.model.entity.Document
import io.brieflyz.document_service.repository.DocumentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono

@Service
class DocumentService(
    private val documentRepository: DocumentRepository,
    private val kafkaSender: KafkaSender
) {
    private val log = logger()

    fun sendDocumentStructureRequest(request: DocumentCreateRequest): Mono<Void> {
        val message = DocumentStructureRequestMessage(
            request.aiProvider,
            request.title,
            request.content,
            request.documentType,
            request.templateName,
            request.sections,
            request.additionalOptions
        )
        return kafkaSender.sendReactive(KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC, message)
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
    fun updateStatus(documentId: String, fileName: String, fileUrl: String, downloadUrl: String): Mono<Void> =
        documentRepository.findById(documentId)
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
                            "URL=${it.fileUrl}"
                )
            }
            .onErrorResume { ex ->
                log.error("Failed to update document for ID=$documentId", ex)
                Mono.empty()
            }
            .then()

    @Transactional(readOnly = true)
    fun getDocumentInfo(documentId: String): Mono<DocumentResponse> =
        documentRepository.findById(documentId).map { it.toResponse() }

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