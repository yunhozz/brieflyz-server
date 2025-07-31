package io.brieflyz.core.config

import io.brieflyz.core.dto.kafka.KafkaMessage
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
@EnableKafka
@EnableAutoConfiguration
class KafkaConfig(
    private val kafkaProperties: KafkaProperties
) {
    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, KafkaMessage> = KafkaTemplate(kafkaProducerFactory())

    @Bean
    fun kafkaProducerFactory(): ProducerFactory<String, KafkaMessage> =
        DefaultKafkaProducerFactory(kafkaProducerProperties())

    @Bean
    fun kafkaConsumerFactory(): ConsumerFactory<String, KafkaMessage> =
        DefaultKafkaConsumerFactory(kafkaConsumerProperties())

    @Bean
    fun kafkaListenerContainerFactory() = ConcurrentKafkaListenerContainerFactory<String, KafkaMessage>().apply {
        consumerFactory = kafkaConsumerFactory()
        containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        setConcurrency(3)
        setCommonErrorHandler(
            DefaultErrorHandler(
                DeadLetterPublishingRecoverer(kafkaTemplate()),
                FixedBackOff(1000, 3)
            )
        )
    }

    @Bean
    fun kafkaProducerProperties() = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.RETRIES_CONFIG to 10,
        ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 1000
    )

    @Bean
    fun kafkaConsumerProperties() = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaProperties.bootstrapServers,
        ConsumerConfig.GROUP_ID_CONFIG to kafkaProperties.consumer.groupId,
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        JsonDeserializer.TRUSTED_PACKAGES to "io.brieflyz.core.*"
    )
}