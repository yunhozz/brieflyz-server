package io.brieflyz.ai_service.service

import io.brieflyz.ai_service.common.enums.AiProvider
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface AiService {
    fun getProvider(): AiProvider
    fun generateContent(prompt: String): Flux<String>
    fun generateStructuredContent(prompt: String, outputFormat: String): Mono<Map<String, Any>>
    fun generateDocumentStructure(title: String, sections: List<String>): Mono<Map<String, String>>
    fun generateExcelStructure(title: String, content: String): Mono<Map<String, List<List<String>>>>
    fun generatePptStructure(title: String, content: String): Mono<List<Map<String, String>>>
}