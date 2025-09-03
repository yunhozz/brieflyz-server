package io.brieflyz.auth_service.application.port.out

import org.thymeleaf.context.Context

interface EmailPort {
    fun send(email: String, context: Context)
}