package io.brieflyz.ai_service.service.support

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentGenerateRequest
import io.brieflyz.ai_service.service.document.DocumentGeneratorFactory
import io.brieflyz.core.dto.kafka.DocumentRequestMessage
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
    fun documentRequestTopicConsumer() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .flatMap { record ->
                val message = record.value() as DocumentRequestMessage
                val documentGenerator = documentGeneratorFactory.createByDocumentType(message.documentType)
                val request = DocumentGenerateRequest(message.title, message.content)

                documentGenerator.generateDocument(AiProvider.OPEN_AI, request)
            }
            .doOnSubscribe { log.info("Start consumer about document request topic") }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}