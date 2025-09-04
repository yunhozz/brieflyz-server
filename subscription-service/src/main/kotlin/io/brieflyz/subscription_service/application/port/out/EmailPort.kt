package io.brieflyz.subscription_service.application.port.out

import java.util.concurrent.CompletableFuture

interface EmailPort {
    fun send(email: String, subject: String, template: String, contextMap: Map<String, Any>): CompletableFuture<Boolean>
}