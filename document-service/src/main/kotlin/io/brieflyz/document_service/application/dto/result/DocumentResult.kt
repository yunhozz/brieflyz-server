package io.brieflyz.document_service.application.dto.result

import io.brieflyz.document_service.common.enums.DocumentStatus
import org.springframework.core.io.Resource
import org.springframework.http.MediaType

data class DocumentResult(
    val documentId: String?,
    val title: String,
    val fileName: String?,
    val fileUrl: String?,
    val downloadUrl: String?,
    val status: DocumentStatus,
    val errorMessage: String?
)

data class DocumentResourceResult(
    val fileName: String,
    val resource: Resource,
    val mediaType: MediaType
)