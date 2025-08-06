package io.brieflyz.ai_service.service.component

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.service.AiService
import org.springframework.stereotype.Component

@Component
class AiServiceFactory(services: Set<AiService>) {

    private val serviceMap: Map<AiProvider, AiService> = services.associateBy { it.getProvider() }

    fun createByProvider(provider: AiProvider): AiService = serviceMap[provider]
        ?: throw IllegalArgumentException("해당 AI 서비스가 존재하지 않습니다.")
}