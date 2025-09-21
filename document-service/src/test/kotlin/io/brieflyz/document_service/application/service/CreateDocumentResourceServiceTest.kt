package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.domain.model.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CreateDocumentResourceServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var service: CreateDocumentResourceService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        service = CreateDocumentResourceService(documentRepositoryPort)
    }

    @Test
    fun `should return document resource when document is completed`() {
        val document = Document("doc1", "user", "title", DocumentType.EXCEL).apply {
            updateForComplete("file.xlsx", "file:///tmp/file.xlsx", "http://localhost/file.xlsx")
            updateStatus(DocumentStatus.COMPLETED, null)
        }

        whenever(documentRepositoryPort.findByDocumentId("doc1")).thenReturn(Mono.just(document))

        val result = service.create("doc1")

        StepVerifier.create(result)
            .assertNext { resource ->
                assert(resource.fileName == "file.xlsx")
            }
            .verifyComplete()

        verify(documentRepositoryPort).findByDocumentId("doc1")
    }

    @Test
    fun `should throw error when document is not completed`() {
        val document = Document("doc1", "user", "title", DocumentType.EXCEL)

        whenever(documentRepositoryPort.findByDocumentId("doc1")).thenReturn(Mono.just(document))

        val result = service.create("doc1")

        StepVerifier.create(result)
            .expectError(IllegalArgumentException::class.java)
            .verify()
    }
}