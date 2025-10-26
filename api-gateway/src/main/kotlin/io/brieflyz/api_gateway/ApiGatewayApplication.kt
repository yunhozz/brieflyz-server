package io.brieflyz.api_gateway

import io.brieflyz.core.beans.JwtBeanScanner
import io.brieflyz.core.beans.MapperBeanScanner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(JwtBeanScanner::class, MapperBeanScanner::class)
class ApiGatewayApplication

fun main(args: Array<String>) {
    runApplication<ApiGatewayApplication>(*args)
}