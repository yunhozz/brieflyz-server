package io.brieflyz.document_service.application.port.`in`

import io.brieflyz.document_service.application.dto.command.CreateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
import io.brieflyz.document_service.application.dto.result.DocumentResourceResult
import io.brieflyz.document_service.application.dto.result.DocumentResult
import io.brieflyz.document_service.domain.model.Document
import reactor.core.publisher.Mono

interface CreateDocumentWithAiUseCase {
    fun create(command: CreateDocumentCommand): Mono<DocumentResult>
}

interface SaveDocumentUseCase {
    fun save(document: Document): Mono<Void>
}

interface UpdateDocumentStatusUseCase {
    fun update(command: UpdateDocumentCommand): Mono<Void>
}

interface UpdateFileInfoUseCase {
    fun update(command: UpdateFileInfoCommand): Mono<Void>
}

interface FindDocumentListUseCase {
    fun findDocumentListByUsername(username: String): Mono<List<DocumentResult>>
}

interface CreateDocumentResourceUseCase {
    fun create(documentId: String): Mono<DocumentResourceResult>
}