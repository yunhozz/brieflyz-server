package io.brieflyz.document_service.domain.model

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.common.enums.DocumentStatus

class Document(
    val documentId: String,
    val username: String,
    val title: String,
    val type: DocumentType
) {
    var status: DocumentStatus = DocumentStatus.PENDING
    var fileName: String? = null
    var fileUrl: String? = null
    var downloadUrl: String? = null
    var errorMessage: String? = null

    fun updateStatus(status: DocumentStatus, errorMessage: String?) {
        this.status = status
        errorMessage?.let { this.errorMessage = it }
    }

    fun updateForComplete(fileName: String, fileUrl: String, downloadUrl: String) {
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.downloadUrl = downloadUrl
        this.status = DocumentStatus.COMPLETED
    }
}