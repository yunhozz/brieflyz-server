package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
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

class UpdateFileInfoServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var service: UpdateFileInfoService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        service = UpdateFileInfoService(documentRepositoryPort)

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
        whenever(documentRepositoryPort.updateFileInfo(any())).thenReturn(Mono.empty())
    }

    @Test
    fun `should update file info successfully`() {
        val command = UpdateFileInfoCommand("doc1", "file.xlsx", "file:///tmp/file.xlsx", "http://localhost/file.xlsx")

        val result = service.update(command)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentRepositoryPort).updateFileInfo(any())
    }
}