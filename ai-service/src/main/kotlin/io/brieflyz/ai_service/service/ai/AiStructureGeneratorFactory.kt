package io.brieflyz.ai_service.service.ai

import io.brieflyz.core.constants.AiProvider
import org.springframework.stereotype.Component

@Component
class AiStructureGeneratorFactory(generators: Set<AiStructureGenerator>) {

    private val generatorMap: Map<AiProvider, AiStructureGenerator> = generators.associateBy { it.getProvider() }

    fun createByProvider(provider: AiProvider): AiStructureGenerator = generatorMap[provider]
        ?: throw IllegalArgumentException("해당 AI 서비스가 존재하지 않습니다.")
}