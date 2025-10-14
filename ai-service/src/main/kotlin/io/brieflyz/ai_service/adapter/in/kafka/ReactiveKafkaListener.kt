package io.brieflyz.ai_service.adapter.`in`.kafka

import io.brieflyz.ai_service.application.dto.command.CreateStructureCommand
import io.brieflyz.ai_service.application.port.`in`.GenerateAiStructureUseCase
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.DocumentStructureRequestMessage
import io.brieflyz.core.dto.message.KafkaMessage
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
    private val generateAiStructureUseCase: GenerateAiStructureUseCase
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
                val command = CreateStructureCommand(
                    message.aiProvider,
                    message.documentId,
                    message.documentType,
                    message.title,
                    message.content
                )
                generateAiStructureUseCase.createStructureAndResponse(command)
            }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(5)))
            .subscribe()
    }
}