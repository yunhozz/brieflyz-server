package io.brieflyz.document_service

import io.brieflyz.core.beans.JwtBeanScanner
import io.brieflyz.core.beans.KafkaBeanScanner
import io.brieflyz.core.beans.MapperBeanScanner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(JwtBeanScanner::class, KafkaBeanScanner::class, MapperBeanScanner::class)
class DocumentServiceApplication

fun main(args: Array<String>) {
    runApplication<DocumentServiceApplication>(*args)
}