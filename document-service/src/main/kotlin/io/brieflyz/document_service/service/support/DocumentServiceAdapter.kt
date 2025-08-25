package io.brieflyz.document_service.service.support

import io.brieflyz.document_service.model.entity.Document
import reactor.core.publisher.Mono
import java.nio.file.Path

interface DocumentServiceAdapter {
    fun save(document: Document): Mono<Void>
    fun updateFileInfo(documentId: String, path: Path, downloadUrl: String): Mono<Void>
}