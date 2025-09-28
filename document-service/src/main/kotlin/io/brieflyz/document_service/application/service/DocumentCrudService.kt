package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.message.DocumentStructureRequestMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.CreateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
import io.brieflyz.document_service.application.dto.result.DocumentResourceResult
import io.brieflyz.document_service.application.dto.result.DocumentResult
import io.brieflyz.document_service.application.port.`in`.CreateDocumentResourceUseCase
import io.brieflyz.document_service.application.port.`in`.CreateDocumentWithAiUseCase
import io.brieflyz.document_service.application.port.`in`.FindDocumentListUseCase
import io.brieflyz.document_service.application.port.`in`.SaveDocumentUseCase
import io.brieflyz.document_service.application.port.`in`.UpdateDocumentStatusUseCase
import io.brieflyz.document_service.application.port.`in`.UpdateFileInfoUseCase
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.application.port.out.MessagePort
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.domain.model.Document
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Mono
import java.net.URI
import java.nio.file.Paths
import java.util.UUID

@Service
class CreateDocumentWithAiService(
    private val documentRepositoryPort: DocumentRepositoryPort,
    private val messagePort: MessagePort
) : CreateDocumentWithAiUseCase {

    @Transactional
    override fun create(command: CreateDocumentCommand): Mono<DocumentResult> {
        val documentId = UUID.randomUUID().toString()
        val document = Document(documentId, command.username, command.title, command.documentType)

        return documentRepositoryPort.save(document)
            .flatMap { document ->
                val (_, aiProvider, _, content, _, templateName, sections, additionalOptions) = command
                val message = DocumentStructureRequestMessage(
                    aiProvider,
                    documentId,
                    title = document.title,
                    content,
                    documentType = document.type,
                    templateName,
                    sections,
                    additionalOptions
                )

                messagePort.sendDocumentStructureRequestMessage(message)
                    .then(Mono.just(document.toResult()))
            }
    }
}

@Service
class SaveDocumentService(
    private val documentRepositoryPort: DocumentRepositoryPort
) : SaveDocumentUseCase {

    private val log = logger()

    @Transactional
    override fun save(document: Document): Mono<Void> =
        documentRepositoryPort.save(document)
            .doOnSuccess { log.info("Document save success. ID=${document.documentId}, title=${document.title}") }
            .onErrorResume { ex ->
                log.error("Failed to save document. Title=${document.title}", ex)
                Mono.empty()
            }
            .then()
}

@Service
class UpdateDocumentStatusService(
    private val documentRepositoryPort: DocumentRepositoryPort
) : UpdateDocumentStatusUseCase {

    private val log = logger()

    @Transactional
    override fun update(command: UpdateDocumentCommand): Mono<Void> =
        documentRepositoryPort.findByDocumentId(command.documentId)
            .flatMap { document ->
                val (_, status, errorMessage) = command
                document.updateStatus(status, errorMessage)
                documentRepositoryPort.updateStatus(document)
            }
            .doOnSuccess { log.info("Update document status success. ID=${command.documentId}, status=${command.status}") }
            .then()
}

@Service
class UpdateFileInfoService(
    private val documentRepositoryPort: DocumentRepositoryPort
) : UpdateFileInfoUseCase {

    private val log = logger()

    @Transactional
    override fun update(command: UpdateFileInfoCommand): Mono<Void> {
        val (documentId, fileName, fileUrl, downloadUrl) = command

        return documentRepositoryPort.findByDocumentId(documentId)
            .flatMap { document ->
                document.updateForComplete(fileName, fileUrl, downloadUrl)
                log.debug("Document info={}", document.toResult())
                documentRepositoryPort.updateFileInfo(document)
            }
            .doOnSuccess {
                log.info(
                    "Document update success. " +
                            "ID=$documentId, " +
                            "file name=$fileName, " +
                            "file URL=$fileUrl, " +
                            "download URL=$downloadUrl"
                )
            }
            .onErrorResume { ex ->
                log.error("Failed to update document for ID=$documentId", ex)
                Mono.empty()
            }
            .then()
    }
}

@Service
class FindDocumentListService(
    private val documentRepositoryPort: DocumentRepositoryPort
) : FindDocumentListUseCase {

    @Transactional(readOnly = true)
    override fun findDocumentListByUsername(username: String): Mono<List<DocumentResult>> =
        documentRepositoryPort.findAllByUsernameOrderByUpdatedAtDesc(username)
            .map { it.toResult() }
            .collectList()
}

@Service
class CreateDocumentResourceService(
    private val documentRepositoryPort: DocumentRepositoryPort
) : CreateDocumentResourceUseCase {

    companion object {
        const val EXCEL_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        const val PPT_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    }

    @Transactional(readOnly = true)
    override fun create(documentId: String): Mono<DocumentResourceResult> =
        documentRepositoryPort.findByDocumentId(documentId)
            .flatMap { document ->
                if (document.status == DocumentStatus.COMPLETED) {
                    val mediaType = when (document.type) {
                        DocumentType.EXCEL -> MediaType.parseMediaType(EXCEL_MEDIA_TYPE)
                        DocumentType.POWERPOINT -> MediaType.parseMediaType(PPT_MEDIA_TYPE)
                    }

                    Mono.just(
                        DocumentResourceResult(
                            fileName = document.fileName!!,
                            resource = FileSystemResource(Paths.get(URI(document.fileUrl!!))),
                            mediaType
                        )
                    )

                } else {
                    Mono.error(IllegalArgumentException("Document is not completed. ID=$documentId, status=${document.status}"))
                }
            }
}

private fun Document.toResult() = DocumentResult(
    documentId = this.documentId.toString(),
    title = this.title,
    fileName = this.fileName,
    fileUrl = this.fileUrl,
    downloadUrl = this.downloadUrl,
    status = this.status,
    errorMessage = this.errorMessage
)