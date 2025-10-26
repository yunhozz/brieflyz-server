package io.brieflyz.document_service.application.dto.command

import io.brieflyz.core.constants.DocumentType

data class DocumentGenerateCommand(
    val documentId: String,
    val title: String,
    val documentType: DocumentType,
    val structure: Any?,
    val errMsg: String?
)