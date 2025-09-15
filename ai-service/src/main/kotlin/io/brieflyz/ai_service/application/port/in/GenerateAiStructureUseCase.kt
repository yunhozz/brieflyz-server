package io.brieflyz.ai_service.application.port.`in`

import io.brieflyz.ai_service.application.dto.command.CreateStructureCommand
import reactor.core.publisher.Mono

interface GenerateAiStructureUseCase {
    fun createStructureAndResponse(command: CreateStructureCommand): Mono<Void>
}