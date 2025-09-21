package io.brieflyz.document_service.adapter.`in`.kafka

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.DocumentStructureResponseMessage
import io.brieflyz.core.dto.message.KafkaMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.DocumentGenerateCommand
import io.brieflyz.document_service.application.port.`in`.DocumentGenerateUseCase
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.stereotype.Component
import reactor.util.retry.Retry
import java.time.Duration

@Component
class ReactiveKafkaListener(
    private val reactiveKafkaConsumerTemplate: ReactiveKafkaConsumerTemplate<String, KafkaMessage>,
    private val documentGenerateUseCase: DocumentGenerateUseCase
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
                val command = DocumentGenerateCommand(
                    documentId = message.documentId,
                    title = message.title,
                    documentType = message.documentType,
                    structure = message.structure,
                    errMsg = message.errMsg
                )

                documentGenerateUseCase.generate(command)
            }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}