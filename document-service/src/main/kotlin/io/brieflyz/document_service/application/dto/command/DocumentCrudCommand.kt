package io.brieflyz.document_service.application.dto.command

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.common.enums.DocumentStatus

data class CreateDocumentCommand(
    val username: String,
    val aiProvider: AiProvider,
    val title: String,
    val content: String,
    val documentType: DocumentType,
    val templateName: String?,
    val sections: List<String>?,
    val additionalOptions: Map<String, Any>?
)

data class UpdateDocumentCommand(
    val documentId: String,
    val status: DocumentStatus,
    val errorMessage: String? = null
)

data class UpdateFileInfoCommand(
    val documentId: String,
    val fileName: String,
    val fileUrl: String,
    val downloadUrl: String
)