package io.brieflyz.ai_service.application.port.`in`

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import reactor.core.publisher.Mono

interface GenerateAiStructureUseCase {
    fun createStructureAndResponse(
        aiProvider: AiProvider,
        documentId: String,
        documentType: DocumentType,
        title: String,
        content: String
    ): Mono<Void>
}