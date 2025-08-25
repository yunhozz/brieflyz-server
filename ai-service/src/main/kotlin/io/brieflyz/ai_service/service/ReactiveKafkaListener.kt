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
    fun documentRequestTopicConsumer() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .filter { it.topic() == KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC }
            .doOnNext { record ->
                log.info("Received message from topic=${record.topic()}, key=${record.key()}")
            }
            .flatMap { record ->
                val message = record.value() as DocumentStructureRequestMessage
                val (aiProvider, documentId, title, content) = message
                val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(aiProvider)

                when (message.documentType) {
                    DocumentType.EXCEL -> aiStructureGenerator.generateExcelStructure(title, content)
                        .flatMap {
                            log.debug("Generate excel structure successfully. document ID=$documentId")
                            sendStructureResponse(documentId, title, DocumentType.EXCEL, it)
                        }

                    DocumentType.POWERPOINT -> aiStructureGenerator.generatePptStructure(title, content)
                        .flatMap {
                            log.debug("Generate PPT structure successfully. document ID=$documentId")
                            sendStructureResponse(documentId, title, DocumentType.POWERPOINT, it)
                        }
                }.doOnSuccess {
                    log.info("Response sent successfully for document. ID=$documentId")
                }.doOnError { ex ->
                    log.error("Error while processing message for document. ID=$documentId", ex)
                }
            }
            .doOnSubscribe { log.info("Start consumer for topic=${KafkaTopic.DOCUMENT_STRUCTURE_REQUEST_TOPIC}") }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }

    private fun sendStructureResponse(
        documentId: String,
        title: String,
        type: DocumentType,
        structure: Any
    ): Mono<Void> {
        val topic = KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC
        log.debug("Sending structure data to topic={}", topic)
        return kafkaSender.sendReactive(topic, DocumentStructureResponseMessage(documentId, title, type, structure))
    }
}