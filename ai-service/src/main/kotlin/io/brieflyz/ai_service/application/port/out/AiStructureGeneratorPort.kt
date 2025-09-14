package io.brieflyz.ai_service.application.port.out

import io.brieflyz.core.constants.AiProvider
import reactor.core.publisher.Mono

interface AiStructureGeneratorPort {
    fun getProvider(): AiProvider
    fun generateDocumentStructure(title: String, sections: List<String>): Mono<Map<String, String>>
    fun generateExcelStructure(title: String, content: String): Mono<Map<String, List<List<String>>>>
    fun generatePptStructure(title: String, content: String): Mono<List<Map<String, String>>>
}