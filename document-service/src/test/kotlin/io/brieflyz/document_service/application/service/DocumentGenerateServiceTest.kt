package io.brieflyz.document_service.application.service

import io.brieflyz.core.constants.DocumentType
import io.brieflyz.document_service.application.dto.command.DocumentGenerateCommand
import io.brieflyz.document_service.application.port.out.DocumentGeneratorPort
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class DocumentGenerateServiceTest {

    private lateinit var documentGeneratorPortFactory: DocumentGeneratorPortFactory
    private lateinit var documentGenerator: DocumentGeneratorPort
    private lateinit var service: DocumentGenerateService

    @BeforeEach
    fun setUp() {
        documentGeneratorPortFactory = mock()
        documentGenerator = mock()
        service = DocumentGenerateService(documentGeneratorPortFactory)

        whenever(documentGeneratorPortFactory.createByDocumentType(any())).thenReturn(documentGenerator)
        whenever(documentGenerator.generateDocument(any(), any(), any())).thenReturn(Mono.empty())
        whenever(documentGenerator.updateDocumentFailed(any(), any())).thenReturn(Mono.empty())
    }

    @Test
    fun `generate should call generateDocument when errMsg is null`() {
        val command = DocumentGenerateCommand(
            documentId = "doc-1",
            title = "Test Document",
            documentType = DocumentType.EXCEL,
            structure = mapOf("sheet" to "data"),
            errMsg = null
        )

        val result = service.generate(command)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentGenerator).generateDocument("doc-1", "Test Document", mapOf("sheet" to "data"))
        verify(documentGenerator, never()).updateDocumentFailed(any(), any())
    }

    @Test
    fun `generate should call generateDocument when errMsg is blank`() {
        val command = DocumentGenerateCommand(
            documentId = "doc-2",
            title = "Another Document",
            documentType = DocumentType.POWERPOINT,
            structure = listOf("slide1", "slide2"),
            errMsg = "   " // blank string
        )

        val result = service.generate(command)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentGenerator).generateDocument("doc-2", "Another Document", listOf("slide1", "slide2"))
        verify(documentGenerator, never()).updateDocumentFailed(any(), any())
    }

    @Test
    fun `generate should call updateDocumentFailed when errMsg is present`() {
        val command = DocumentGenerateCommand(
            documentId = "doc-3",
            title = "Failed Document",
            documentType = DocumentType.EXCEL,
            structure = null,
            errMsg = "AI error occurred"
        )

        val result = service.generate(command)

        StepVerifier.create(result)
            .verifyComplete()

        verify(documentGenerator).updateDocumentFailed("doc-3", "AI error occurred")
        verify(documentGenerator, never()).generateDocument(any(), any(), anyOrNull())
    }
}