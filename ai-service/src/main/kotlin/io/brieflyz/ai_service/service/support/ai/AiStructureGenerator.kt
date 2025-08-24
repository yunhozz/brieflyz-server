package io.brieflyz.ai_service.service.support.ai

import io.brieflyz.ai_service.common.enums.AiProvider
import reactor.core.publisher.Mono

interface AiStructureGenerator {
    fun getProvider(): AiProvider
    fun generateDocumentStructure(title: String, sections: List<String>): Mono<Map<String, String>>
    fun generateExcelStructure(title: String, content: String): Mono<Map<String, List<List<String>>>>
    fun generatePptStructure(title: String, content: String): Mono<List<Map<String, String>>>
}