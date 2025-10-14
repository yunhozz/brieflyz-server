package io.brieflyz.ai_service.application.service

import io.brieflyz.ai_service.application.dto.command.CreateStructureCommand
import io.brieflyz.ai_service.application.port.out.AiStructureGeneratorPort
import io.brieflyz.ai_service.application.port.out.MessagePort
import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.document.ExcelStructure
import io.brieflyz.core.dto.document.PowerPointStructure
import io.brieflyz.core.dto.document.Row
import io.brieflyz.core.dto.document.Slide
import io.brieflyz.core.dto.message.DocumentStructureResponseMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class GenerateAiStructureServiceTest {

    private lateinit var messagePort: MessagePort
    private lateinit var aiStructureGeneratorPortFactory: AiStructureGeneratorPortFactory
    private lateinit var aiStructureGeneratorPort: AiStructureGeneratorPort
    private lateinit var service: GenerateAiStructureService

    @BeforeEach
    fun setUp() {
        messagePort = mock()
        aiStructureGeneratorPortFactory = mock()
        aiStructureGeneratorPort = mock()
        service = GenerateAiStructureService(messagePort, aiStructureGeneratorPortFactory)

        whenever(aiStructureGeneratorPortFactory.createByProvider(any())).thenReturn(aiStructureGeneratorPort)
        whenever(messagePort.sendDocumentStructureResponseMessage(any())).thenReturn(Mono.empty<Any>())
    }

    @Test
    fun `createStructureAndResponse - success with EXCEL`() {
        val command = CreateStructureCommand(
            aiProvider = AiProvider.OPEN_AI,
            documentId = "test",
            documentType = DocumentType.EXCEL,
            title = "Sample Excel",
            content = "some content"
        )

        val excelStructure = ExcelStructure(mapOf("sheet1" to listOf(Row(listOf("row1", "row2")))))

        whenever(aiStructureGeneratorPort.generateExcelStructure(command.title, command.content))
            .thenReturn(Mono.just(excelStructure))

        val result = service.createStructureAndResponse(command)

        StepVerifier.create(result).verifyComplete()

        argumentCaptor<DocumentStructureResponseMessage>().apply {
            verify(messagePort).sendDocumentStructureResponseMessage(capture())
            assert(firstValue.documentId == "test")
            assert(firstValue.structure == excelStructure)
            assert(firstValue.errMsg == null)
        }
    }

    @Test
    fun `createStructureAndResponse - success with POWERPOINT`() {
        val command = CreateStructureCommand(
            aiProvider = AiProvider.OPEN_AI,
            documentId = "test",
            documentType = DocumentType.POWERPOINT,
            title = "Sample PPT",
            content = "ppt content"
        )

        val powerPointStructure = PowerPointStructure(listOf(mapOf("slide1" to Slide("title", "content", "notes"))))

        whenever(aiStructureGeneratorPort.generatePptStructure(command.title, command.content))
            .thenReturn(Mono.just(powerPointStructure))

        val result = service.createStructureAndResponse(command)

        StepVerifier.create(result)
            .verifyComplete()

        argumentCaptor<DocumentStructureResponseMessage>().apply {
            verify(messagePort).sendDocumentStructureResponseMessage(capture())
            assert(firstValue.documentId == "test")
            assert(firstValue.structure == powerPointStructure)
            assert(firstValue.errMsg == null)
        }
    }

    @Test
    fun `createStructureAndResponse - failure case`() {
        val command = CreateStructureCommand(
            aiProvider = AiProvider.OPEN_AI,
            documentId = "test",
            documentType = DocumentType.EXCEL,
            title = "Broken Excel",
            content = "invalid content"
        )

        whenever(aiStructureGeneratorPort.generateExcelStructure(command.title, command.content))
            .thenReturn(Mono.error(RuntimeException("Generation failed")))

        val result = service.createStructureAndResponse(command)

        StepVerifier.create(result)
            .verifyComplete()

        argumentCaptor<DocumentStructureResponseMessage>().apply {
            verify(messagePort).sendDocumentStructureResponseMessage(capture())
            assert(firstValue.documentId == "test")
            assert(firstValue.structure == null)
            assert(firstValue.errMsg!!.contains("Generation failed"))
        }
    }
}