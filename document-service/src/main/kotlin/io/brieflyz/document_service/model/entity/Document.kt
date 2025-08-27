package io.brieflyz.document_service.model.entity

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.common.enums.DocumentStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table
class Document(
    @Id
    val documentId: String?,
    val username: String,
    val title: String,
    val type: DocumentType
) : Persistable<String> {

    var status: DocumentStatus = DocumentStatus.PENDING
        protected set

    var fileName: String? = null
        protected set

    var fileUrl: String? = null
        protected set

    var downloadUrl: String? = null
        protected set

    var errorMessage: String? = null
        protected set

    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        protected set

    override fun getId(): String? = documentId

    override fun isNew(): Boolean = createdAt == null

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