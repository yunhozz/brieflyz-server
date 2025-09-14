package io.brieflyz.ai_service.application.service

import io.brieflyz.ai_service.application.port.`in`.GenerateAiStructureUseCase
import io.brieflyz.ai_service.application.port.out.MessagePort
import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.message.DocumentStructureResponseMessage
import io.brieflyz.core.utils.logger
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GenerateAiStructureService(
    private val messagePort: MessagePort,
    private val aiStructureGeneratorPortFactory: AiStructureGeneratorPortFactory
) : GenerateAiStructureUseCase {

    private val log = logger()

    override fun createStructureAndResponse(
        aiProvider: AiProvider,
        documentId: String,
        documentType: DocumentType,
        title: String,
        content: String
    ): Mono<Void> {
        val aiStructureGeneratorPort = aiStructureGeneratorPortFactory.createByProvider(aiProvider)
        return when (documentType) {
            DocumentType.EXCEL -> aiStructureGeneratorPort.generateExcelStructure(title, content)
            DocumentType.POWERPOINT -> aiStructureGeneratorPort.generatePptStructure(title, content)
        }.flatMap { structure ->
            log.info("Response sent successfully for document. ID=$documentId")
            sendStructureResponseMessage(documentId, title, documentType, structure)
        }.onErrorResume { ex ->
            log.error("Error while processing message for document. ID=$documentId", ex)
            sendStructureResponseMessage(documentId, title, documentType, null, ex.message)
        }
    }

    private fun sendStructureResponseMessage(
        documentId: String,
        title: String,
        type: DocumentType,
        structure: Any?,
        errMsg: String? = null
    ): Mono<Void> {
        val message = DocumentStructureResponseMessage(documentId, title, type, structure, errMsg)
        return messagePort.sendDocumentStructureResponseMessage(message).then()
    }
}