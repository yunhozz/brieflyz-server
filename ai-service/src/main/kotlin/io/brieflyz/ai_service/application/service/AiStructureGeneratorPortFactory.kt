package io.brieflyz.ai_service.application.service

import io.brieflyz.ai_service.application.port.out.AiStructureGeneratorPort
import io.brieflyz.core.constants.AiProvider
import org.springframework.stereotype.Component

@Component
class AiStructureGeneratorPortFactory(ports: Set<AiStructureGeneratorPort>) {

    private val portMap: Map<AiProvider, AiStructureGeneratorPort> = ports.associateBy { it.getProvider() }

    fun createByProvider(provider: AiProvider): AiStructureGeneratorPort = portMap[provider]
        ?: throw IllegalArgumentException("해당 AI 서비스가 존재하지 않습니다.")
}