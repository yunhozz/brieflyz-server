package io.brieflyz.ai_service.model.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class DocumentResponse private constructor(
    val documentId: String,
    val title: String,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val downloadUrl: String? = null,
    val status: DocumentStatus,
    val createdAt: LocalDateTime? = null,
    val errorMessage: String? = null
) {
    companion object {
        fun forProcessing(documentId: String, title: String) =
            DocumentResponse(
                documentId,
                title,
                status = DocumentStatus.PROCESSING
            )

        fun forCompleted(documentId: String, title: String, fileName: String, fileUrl: String, downloadUrl: String) =
            DocumentResponse(
                documentId,
                title,
                fileName,
                fileUrl,
                downloadUrl,
                status = DocumentStatus.COMPLETED
            )

        fun forFailed(documentId: String, title: String, errorMessage: String) =
            DocumentResponse(
                documentId,
                title,
                errorMessage,
                status = DocumentStatus.FAILED
            )
    }

    enum class DocumentStatus {
        PROCESSING,
        COMPLETED,
        FAILED
    }
}