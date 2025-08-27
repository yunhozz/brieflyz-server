package io.brieflyz.document_service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.brieflyz.document_service.common.enums.DocumentStatus
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentResponse(
    val documentId: String?,
    val title: String,
    val fileName: String?,
    val fileUrl: String?,
    val downloadUrl: String?,
    val status: DocumentStatus,
    val errorMessage: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)