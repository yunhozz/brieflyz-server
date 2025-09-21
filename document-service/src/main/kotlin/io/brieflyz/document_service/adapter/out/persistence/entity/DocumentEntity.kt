package io.brieflyz.document_service.adapter.out.persistence.entity

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.common.enums.DocumentStatus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "document")
data class DocumentEntity(
    @Id
    var documentId: String? = null,
    val username: String,
    val title: String,
    val type: DocumentType,
    var status: DocumentStatus,
    var fileName: String?,
    var fileUrl: String?,
    var downloadUrl: String?,
    var errorMessage: String?
) : Persistable<String> {

    @CreatedDate
    var createdAt: LocalDateTime? = null

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null

    override fun getId(): String? = documentId

    override fun isNew(): Boolean = createdAt == null
}