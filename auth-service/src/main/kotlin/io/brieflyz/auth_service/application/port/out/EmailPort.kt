package io.brieflyz.auth_service.application.port.out

interface EmailPort {
    fun send(email: String, contextMap: Map<String, Any>)
}