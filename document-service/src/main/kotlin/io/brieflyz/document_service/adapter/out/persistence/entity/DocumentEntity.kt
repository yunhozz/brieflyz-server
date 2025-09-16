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
class DocumentEntity(
    @Id
    val documentId: String?,
    val username: String,
    val title: String,
    val type: DocumentType,
    val status: DocumentStatus,
    val fileName: String?,
    val fileUrl: String?,
    val downloadUrl: String?,
    val errorMessage: String?
) : Persistable<String> {

    @CreatedDate
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    var updatedAt: LocalDateTime? = null
        protected set

    override fun getId(): String? = documentId

    override fun isNew(): Boolean = createdAt == null
}