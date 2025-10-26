package io.brieflyz.document_service.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PropertyConfig {
    @Bean
    @ConfigurationProperties(prefix = "app.document")
    fun documentServiceProperties() = DocumentServiceProperties()
}

data class DocumentServiceProperties(
    var kafka: KafkaProperties = KafkaProperties(),
    var file: FileProperties = FileProperties()
) {
    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class FileProperties(
        var filePath: String = "",
        var downloadUrl: String = ""
    )
}