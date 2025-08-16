package io.brieflyz.ai_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    fun aiServiceProperties() = AiServiceProperties()
}

@ConfigurationProperties(prefix = "app.ai")
@EnableConfigurationProperties(AiServiceProperties::class)
data class AiServiceProperties(
    var kafka: KafkaProperties? = null,
    var file: FileProperties? = null
) {
    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class FileProperties(
        var filePath: String? = null,
        var downloadUrl: String? = null
    )
}