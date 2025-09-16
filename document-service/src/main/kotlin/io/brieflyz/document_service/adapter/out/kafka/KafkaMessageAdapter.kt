package io.brieflyz.document_service.adapter.out.kafka

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.message.DocumentStructureResponseMessage
import io.brieflyz.core.dto.message.FailedKafkaMessage
import io.brieflyz.core.dto.message.KafkaMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.port.out.MessagePort
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kafka.sender.SenderResult
import reactor.util.retry.Retry
import java.time.Duration

@Component
class KafkaMessageAdapter(
    private val reactiveKafkaProducerTemplate: ReactiveKafkaProducerTemplate<String, KafkaMessage>
) : MessagePort {

    private val log = logger()

    override fun sendDocumentStructureRequestMessage(message: Any): Mono<SenderResult<Void>> {
        require(message is DocumentStructureResponseMessage)
        return reactiveKafkaProducerTemplate.send(KafkaTopic.DOCUMENT_STRUCTURE_RESPONSE_TOPIC, message)
            .doOnNext { result ->
                val metadata = result.recordMetadata()
                log.debug(
                    "[Kafka Send Success] topic={}, partition={}, offset={}",
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
                )
            }
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
            .onErrorResume { ex ->
                log.error("Unable to send message due to : ${ex.message}", ex)
                val failedKafkaMessage = FailedKafkaMessage(
                    originalMessage = message.toString(),
                    errorMessage = ex.localizedMessage,
                    timestamp = System.currentTimeMillis()
                )
                reactiveKafkaProducerTemplate.send(KafkaTopic.DEAD_LETTER_TOPIC, failedKafkaMessage)
            }
    }
}