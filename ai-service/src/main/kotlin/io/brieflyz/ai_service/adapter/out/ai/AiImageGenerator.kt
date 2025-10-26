package io.brieflyz.ai_service.adapter.out.ai

import reactor.core.publisher.Mono

interface AiImageGenerator {
    fun generateImageUrl(prompt: String): Mono<String>
}