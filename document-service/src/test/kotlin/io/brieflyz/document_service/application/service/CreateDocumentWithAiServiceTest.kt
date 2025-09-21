package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.message.DocumentStructureRequestMessage
import io.brieflyz.document_service.application.dto.command.CreateDocumentCommand
import io.brieflyz.document_service.application.port.out.DocumentRepositoryPort
import io.brieflyz.document_service.application.port.out.MessagePort
import io.brieflyz.document_service.domain.model.Document
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class CreateDocumentWithAiServiceTest {

    private lateinit var documentRepositoryPort: DocumentRepositoryPort
    private lateinit var messagePort: MessagePort
    private lateinit var service: CreateDocumentWithAiService

    @BeforeEach
    fun setUp() {
        documentRepositoryPort = mock()
        messagePort = mock()
        service = CreateDocumentWithAiService(documentRepositoryPort, messagePort)

        whenever(documentRepositoryPort.save(any()))
            .thenAnswer { Mono.just(it.arguments[0] as Document) }
        whenever(messagePort.sendDocumentStructureRequestMessage(any())).thenReturn(Mono.empty<Any>())
    }

    @Test
    fun `should save document and send structure request`() {
        val command = CreateDocumentCommand(
            username = "test@brieflyz.io",
            aiProvider = AiProvider.OPEN_AI,
            title = "Test Doc",
            content = "Some content",
            documentType = DocumentType.EXCEL,
            templateName = null,
            sections = emptyList(),
            additionalOptions = emptyMap()
        )

        val result = service.create(command)

        StepVerifier.create(result)
            .assertNext { docResult ->
                assert(docResult.title == "Test Doc")
                verify(documentRepositoryPort).save(any())
                verify(messagePort).sendDocumentStructureRequestMessage(any<DocumentStructureRequestMessage>())
            }
            .verifyComplete()
    }
}