package io.brieflyz.document_service.adapter.out.persistence.repository

import io.brieflyz.document_service.adapter.out.persistence.entity.DocumentEntity
import io.brieflyz.document_service.common.enums.DocumentStatus
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface DocumentR2dbcRepository : R2dbcRepository<DocumentEntity, String> {
    fun findAllByUsernameOrderByUpdatedAtDesc(username: String): Flux<DocumentEntity>

    @Modifying
    @Query(
        """
        UPDATE document
        SET status = :status, error_message = :errorMessage
        WHERE document_id = :documentId
    """
    )
    fun updateStatus(status: DocumentStatus, errorMessage: String?, documentId: String): Mono<Int>

    @Modifying
    @Query(
        """
        UPDATE document 
        SET file_name = :fileName, file_url = :fileUrl, download_url = :downloadUrl, status = 'COMPLETED'
        WHERE document_id = :documentId
    """
    )
    fun updateFileInfo(
        documentId: String,
        fileName: String?,
        fileUrl: String?,
        downloadUrl: String?
    ): Mono<Int>
}