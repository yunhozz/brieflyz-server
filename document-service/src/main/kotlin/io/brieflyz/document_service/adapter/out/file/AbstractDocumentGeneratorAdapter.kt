package io.brieflyz.document_service.adapter.out.file

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.port.`in`.UpdateDocumentStatusUseCase
import io.brieflyz.document_service.application.port.out.DocumentGeneratorPort
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.config.DocumentServiceProperties
import reactor.core.publisher.Mono
import java.nio.file.Path
import java.nio.file.Paths

abstract class AbstractDocumentGeneratorAdapter(
    private val updateDocumentStatusUseCase: UpdateDocumentStatusUseCase,
    private val props: DocumentServiceProperties
) : DocumentGeneratorPort {

    private val log = logger()

    override fun updateDocumentFailed(documentId: String, errMsg: String): Mono<Void> {
        log.warn("Failed to generate document. Reason : $errMsg")
        val command = UpdateDocumentCommand(documentId, DocumentStatus.FAILED, errMsg)
        return updateDocumentStatusUseCase.update(command).then()
    }

    protected fun createFilePath(title: String): Path {
        val filePath = props.file.filePath
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = System.nanoTime()

        val format = when (getDocumentType()) {
            DocumentType.WORD -> "docx"
            DocumentType.EXCEL -> "xlsx"
            DocumentType.POWERPOINT -> "pptx"
        }

        return Paths.get(filePath, "${titleName}_$timestamp.$format")
    }
}