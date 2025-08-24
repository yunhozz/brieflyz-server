package io.brieflyz.ai_service.service

import io.brieflyz.ai_service.model.dto.DocumentCreateRequest
import io.brieflyz.ai_service.service.support.ai.AiStructureGeneratorFactory
import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.DocumentCreateRequestMessage
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AiService(
    private val aiStructureGeneratorFactory: AiStructureGeneratorFactory,
    private val kafkaSender: KafkaSender
) {
    fun createDocumentStructure(request: DocumentCreateRequest): Mono<String> {
        val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(request.aiProvider)
        val (_, title, content) = request

        return when (request.documentType) {
            DocumentType.EXCEL -> aiStructureGenerator.generateExcelStructure(title, content)
                .doOnNext { excelStructure ->
                    kafkaSender.sendReactive(
                        KafkaTopic.DOCUMENT_CREATE_REQUEST_TOPIC,
                        DocumentCreateRequestMessage(title, DocumentType.EXCEL, excelStructure)
                    )
                }

            DocumentType.POWERPOINT -> aiStructureGenerator.generatePptStructure(title, content)
                .doOnNext { pptStructure ->
                    kafkaSender.sendReactive(
                        KafkaTopic.DOCUMENT_CREATE_REQUEST_TOPIC,
                        DocumentCreateRequestMessage(title, DocumentType.POWERPOINT, pptStructure)
                    )
                }
        }.then(Mono.just("문서 생성 중입니다. 조금만 기다려주세요."))
    }
}