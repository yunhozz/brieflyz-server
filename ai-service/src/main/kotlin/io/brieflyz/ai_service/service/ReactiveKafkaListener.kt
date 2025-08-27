package io.brieflyz.ai_service.service

import io.brieflyz.ai_service.service.ai.AiStructureGeneratorFactory
import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.DocumentStructureRequestMessage
import io.brieflyz.core.dto.kafka.DocumentStructureResponseMessage
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

@Component
class ReactiveKafkaListener(
    private val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val aiStructureGeneratorFactory: AiStructureGeneratorFactory,
    private val kafkaSender: KafkaSender
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .filter { it.topic() == KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC }
            .doOnSubscribe { log.info("Start consumer for topic=${KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC}") }
            .doOnNext { record ->
                log.info("Received message from topic=${record.topic()}, key=${record.key()}")
            }
            .flatMap { record ->
                val message = record.value() as DocumentStructureRequestMessage
                val (aiProvider, documentId, title, content, documentType) = message
                val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(aiProvider)

                when (documentType) {
                    DocumentType.EXCEL -> aiStructureGenerator.generateExcelStructure(title, content)
                    DocumentType.POWERPOINT -> aiStructureGenerator.generatePptStructure(title, content)
                }.flatMap { structure ->
                    log.info("Response sent successfully for document. ID=$documentId")
                    sendStructureResponse(documentId, title, documentType, structure)
                }.onErrorResume { ex ->
                    log.error("Error while processing message for document. ID=$documentId", ex)
                    sendStructureResponse(documentId, title, documentType, null, ex.message)
                }
            }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }

    private fun sendStructureResponse(
        documentId: String,
        title: String,
        type: DocumentType,
        structure: Any?,
        errMsg: String? = null
    ): Mono<Void> = kafkaSender.sendReactive(
        KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC,
        DocumentStructureResponseMessage(documentId, title, type, structure, errMsg)
    ).then()
}