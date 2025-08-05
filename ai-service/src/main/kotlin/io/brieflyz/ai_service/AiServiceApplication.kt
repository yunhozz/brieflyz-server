package io.brieflyz.ai_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["io.brieflyz.core.config"])
class AiServiceApplication

fun main(args: Array<String>) {
    runApplication<AiServiceApplication>(*args)
}