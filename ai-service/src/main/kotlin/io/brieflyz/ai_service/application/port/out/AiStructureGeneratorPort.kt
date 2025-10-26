package io.brieflyz.ai_service.application.port.out

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.dto.document.ExcelStructure
import io.brieflyz.core.dto.document.PowerPointStructure
import io.brieflyz.core.dto.document.WordStructure
import reactor.core.publisher.Mono

interface AiStructureGeneratorPort {
    fun getProvider(): AiProvider
    fun generateWordStructure(title: String, content: String): Mono<WordStructure>
    fun generateExcelStructure(title: String, content: String): Mono<ExcelStructure>
    fun generatePptStructure(title: String, content: String): Mono<PowerPointStructure>
}