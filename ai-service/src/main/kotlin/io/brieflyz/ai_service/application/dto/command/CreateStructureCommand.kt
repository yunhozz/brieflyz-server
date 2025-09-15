package io.brieflyz.ai_service.application.dto.command

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType

data class CreateStructureCommand(
    val aiProvider: AiProvider,
    val documentId: String,
    val documentType: DocumentType,
    val title: String,
    val content: String
)