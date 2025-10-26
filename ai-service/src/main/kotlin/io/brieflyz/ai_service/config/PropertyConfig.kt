package io.brieflyz.ai_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.ai")
    fun aiServiceProperties() = AiServiceProperties()
}

data class AiServiceProperties(
    var kafka: KafkaProperties = KafkaProperties(),
    var image: ImageProperties = ImageProperties()
) {
    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class ImageProperties(
        var url: String = "",
        var secret: String = ""
    )
}