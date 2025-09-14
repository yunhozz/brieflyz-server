package io.brieflyz.ai_service.application.port.out

import reactor.core.publisher.Mono

interface MessagePort {
    fun sendDocumentStructureResponseMessage(message: Any): Mono<*>
}