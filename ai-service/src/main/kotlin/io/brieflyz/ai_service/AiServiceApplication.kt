package io.brieflyz.ai_service

import io.brieflyz.core.beans.CommonBeanScanner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(CommonBeanScanner::class)
class AiServiceApplication

fun main(args: Array<String>) {
    runApplication<AiServiceApplication>(*args)
}