package io.brieflyz.document_service.service.support

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.DocumentStructureResponseMessage
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.util.retry.Retry
import java.time.Duration

@Component
class ReactiveKafkaListener(
    private val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val documentGeneratorFactory: DocumentGeneratorFactory
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .filter { it.topic() == KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC }
            .doOnSubscribe { log.info("Start consumer for topic=${KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC}") }
            .doOnNext { record ->
                log.info("Received message from topic=${record.topic()}, key=${record.key()}")
            }
            .flatMap { record ->
                val message = record.value() as DocumentStructureResponseMessage
                val (documentId, title, documentType, structure, errMsg) = message
                val documentGenerator = documentGeneratorFactory.createByDocumentType(documentType)

                if (errMsg.isNullOrBlank()) {
                    log.info("Start generating document. ID=$documentId, title=$title")
                    documentGenerator.generateDocument(documentId, title, structure)
                } else {
                    log.error("Error while generating structure from AI server. Error message=$errMsg")
                    documentGenerator.updateDocumentFailed(documentId, errMsg)
                }
            }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}