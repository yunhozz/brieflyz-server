package io.brieflyz.document_service.service

import io.brieflyz.core.dto.kafka.DocumentCreateRequestMessage
import io.brieflyz.core.dto.kafka.KafkaMessage
import io.brieflyz.core.utils.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.util.retry.Retry
import java.time.Duration

@Component
class DocumentKafkaListener(
    private val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val documentGeneratorFactory: DocumentGeneratorFactory
) {
    private val log = logger()

    @EventListener(ApplicationReadyEvent::class)
    fun documentRequestTopicConsumer() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .flatMap { record ->
                val message = record.value() as DocumentCreateRequestMessage
                val documentGenerator = documentGeneratorFactory.createByDocumentType(message.documentType)
                documentGenerator.generateDocument(message.title, message.structure)
            }
            .doOnSubscribe { log.info("Start consumer about document request topic") }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}