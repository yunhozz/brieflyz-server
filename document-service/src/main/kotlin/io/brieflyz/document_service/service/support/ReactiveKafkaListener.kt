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
    fun documentRequestTopicConsumer() {
        reactiveKafkaConsumerTemplate.receiveAutoAck()
            .filter { it.topic() == KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC }
            .doOnNext { record ->
                log.info("Received message from topic=${record.topic()}, key=${record.key()}")
            }
            .flatMap { record ->
                val message = record.value() as DocumentStructureResponseMessage
                val documentGenerator = documentGeneratorFactory.createByDocumentType(message.documentType)

                documentGenerator.generateDocument(message.title, message.structure)
                    .doOnSuccess {
                        log.info("Generate document successfully for title=${message.title}")
                    }
                    .doOnError { ex ->
                        log.error("Error while generating document for title=${message.title}", ex)
                    }
            }
            .doOnSubscribe { log.info("Start consumer for topic=${KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC}") }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}