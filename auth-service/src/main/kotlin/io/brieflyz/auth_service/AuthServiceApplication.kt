package io.brieflyz.auth_service

import io.brieflyz.core.beans.JwtBeanScanner
import io.brieflyz.core.beans.KafkaBeanScanner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(JwtBeanScanner::class, KafkaBeanScanner::class)
class AuthServiceApplication

fun main(args: Array<String>) {
    runApplication<AuthServiceApplication>(*args)
}