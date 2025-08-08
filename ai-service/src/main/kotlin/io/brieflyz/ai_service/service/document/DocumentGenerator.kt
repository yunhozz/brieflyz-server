package io.brieflyz.ai_service.service.document

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.kafka.DocumentRequestMessage
import reactor.core.publisher.Mono
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface DocumentGenerator {
    fun getDocumentType(): DocumentType
    fun generateDocument(aiProvider: AiProvider, request: DocumentRequestMessage): Mono<DocumentResponse>

    fun createFilePath(title: String, path: String): Path {
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return Paths.get(path, "${titleName}_$timestamp.xlsx")
    }
}