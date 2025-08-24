package io.brieflyz.document_service.model.entity

import io.brieflyz.document_service.common.enums.DocumentStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table
class Document private constructor(
    @Id
    val documentId: String?,
    val title: String
) : Persistable<String> {

    companion object {
        fun forProcessing(documentId: String, title: String) = Document(documentId, title)

        fun forFailed(documentId: String, title: String, errorMessage: String): Document {
            val document = Document(documentId, title)
            document.errorMessage = errorMessage
            document.status = DocumentStatus.FAILED
            return document
        }
    }

    var status: DocumentStatus = DocumentStatus.PROCESSING
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

    fun updateForComplete(fileName: String, fileUrl: String, downloadUrl: String) {
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.downloadUrl = downloadUrl
        status = DocumentStatus.COMPLETED
    }
}