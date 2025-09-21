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
import reactor.core.publisher.Flux
import reactor.test.StepVerifier

class FindDocumentListServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var service: FindDocumentListService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        service = FindDocumentListService(documentRepositoryPort)

        whenever(documentRepositoryPort.findAllByUsernameOrderByUpdatedAtDesc(any()))
            .thenReturn(Flux.just(Document("doc1", "user", "title", DocumentType.EXCEL)))
    }

    @Test
    fun `should return document list for username`() {
        val result = service.findDocumentListByUsername("user")

        StepVerifier.create(result)
            .assertNext { list ->
                assert(list.size == 1)
                assert(list[0].title == "title")
            }
            .verifyComplete()

        verify(documentRepositoryPort).findAllByUsernameOrderByUpdatedAtDesc("user")
    }
}