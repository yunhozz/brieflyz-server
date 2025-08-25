package io.brieflyz.document_service.service.support.impl

import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.model.entity.Document
import io.brieflyz.document_service.service.DocumentService
import io.brieflyz.document_service.service.support.DocumentServiceAdapter
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path

@Component
class DocumentServiceAdapterImpl(
    private val documentService: DocumentService
) : DocumentServiceAdapter {

    override fun save(document: Document): Mono<Void> =
        documentService.save(document)

    override fun updateDocumentStatus(documentId: String, status: DocumentStatus, errorMessage: String?): Mono<Void> =
        documentService.updateDocumentStatus(documentId, status, errorMessage)

    override fun updateFileInfo(documentId: String, path: Path, downloadUrl: String): Mono<Void> {
        val fileName = path.fileName.toString()
        val fileUrl = URLDecoder.decode(path.toUri().toURL().toString(), StandardCharsets.UTF_8)
        return documentService.updateFileInfo(documentId, fileName, fileUrl, downloadUrl)
    }
}