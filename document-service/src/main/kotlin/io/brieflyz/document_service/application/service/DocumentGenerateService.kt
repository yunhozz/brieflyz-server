package io.brieflyz.document_service.application.service

import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.DocumentGenerateCommand
import io.brieflyz.document_service.application.port.`in`.DocumentGenerateUseCase
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DocumentGenerateService(
    private val documentGeneratorPortFactory: DocumentGeneratorPortFactory
) : DocumentGenerateUseCase {

    private val log = logger()

    override fun generate(command: DocumentGenerateCommand): Mono<Void> {
        val (documentId, title, documentType, structure, errMsg) = command
        val documentGenerator = documentGeneratorPortFactory.createByDocumentType(documentType)

        return if (errMsg.isNullOrBlank()) {
            log.info("Start generating document. ID=$documentId, title=$title")
            documentGenerator.generateDocument(documentId, title, structure)
        } else {
            log.error("Error while generating structure from AI server. Error message=$errMsg")
            documentGenerator.updateDocumentFailed(documentId, errMsg)
        }
    }
}