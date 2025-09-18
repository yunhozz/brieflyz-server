package io.brieflyz.document_service.domain.model

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.common.enums.DocumentStatus
import java.time.LocalDateTime

class Document(
    val documentId: String,
    val username: String,
    val title: String,
    val type: DocumentType,
    status: DocumentStatus = DocumentStatus.PENDING,
    fileName: String? = null,
    fileUrl: String? = null,
    downloadUrl: String? = null,
    errorMessage: String? = null,
    val createdAt: LocalDateTime? = null
) {
    var status: DocumentStatus = status
        protected set
    var fileName: String? = fileName
        protected set
    var fileUrl: String? = fileUrl
        protected set
    var downloadUrl: String? = downloadUrl
        protected set
    var errorMessage: String? = errorMessage
        protected set

    fun updateStatus(status: DocumentStatus, errorMessage: String?) {
        this.status = status
        errorMessage?.let { this.errorMessage = it }
    }

    fun updateForComplete(fileName: String, fileUrl: String, downloadUrl: String) {
        this.status = DocumentStatus.COMPLETED
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.downloadUrl = downloadUrl
    }
}