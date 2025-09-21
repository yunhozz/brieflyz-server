package io.brieflyz.document_service.application.port.out

import reactor.core.publisher.Mono

interface MessagePort {
    fun sendDocumentStructureRequestMessage(message: Any): Mono<*>
}