package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.domain.model.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class SaveDocumentServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var service: SaveDocumentService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        service = SaveDocumentService(documentRepositoryPort)

        whenever(documentRepositoryPort.save(any())).thenAnswer { Mono.just(it.arguments[0] as Document) }
    }

    @Test
    fun `should save document successfully`() {
        val document = Document("doc1", "user", "title", DocumentType.EXCEL)

        val result = service.save(document)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentRepositoryPort).save(document)
    }
}