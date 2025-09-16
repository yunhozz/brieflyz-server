package io.brieflyz.document_service.application.port.`in`

import io.brieflyz.document_service.application.dto.command.DocumentGenerateCommand
import reactor.core.publisher.Mono

interface DocumentGenerateUseCase {
    fun generate(command: DocumentGenerateCommand): Mono<Void>
}