package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.domain.model.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UpdateDocumentStatusServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var service: UpdateDocumentStatusService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        service = UpdateDocumentStatusService(documentRepositoryPort)

        whenever(documentRepositoryPort.findByDocumentId(any())).thenReturn(
            Mono.just(
                Document(
                    "doc1",
                    "user",
                    "title",
                    DocumentType.EXCEL
                )
            )
        )
        whenever(documentRepositoryPort.updateStatus(any())).thenReturn(Mono.empty())
    }

    @Test
    fun `should update document status`() {
        val command = UpdateDocumentCommand("doc1", DocumentStatus.COMPLETED, null)

        val result = service.update(command)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentRepositoryPort).updateStatus(any())
    }
}