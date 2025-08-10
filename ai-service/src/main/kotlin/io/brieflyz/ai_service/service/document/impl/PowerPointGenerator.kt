package io.brieflyz.ai_service.service.document.impl

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.entity.Document
import io.brieflyz.ai_service.service.ai.AiStructureGeneratorFactory
import io.brieflyz.ai_service.service.document.DocumentGenerator
import io.brieflyz.ai_service.service.document.DocumentManager
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.kafka.DocumentRequestMessage
import io.brieflyz.core.utils.logger
import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.xslf.usermodel.SlideLayout
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.awt.Color
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Component
class PowerPointGenerator(
    private val aiStructureGeneratorFactory: AiStructureGeneratorFactory,
    private val documentManager: DocumentManager
) : DocumentGenerator {

    private val log = logger()

    override fun getDocumentType() = DocumentType.POWERPOINT

    override fun generateDocument(aiProvider: AiProvider, request: DocumentRequestMessage): Mono<Void> {
        val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(aiProvider)
        val documentId = UUID.randomUUID().toString()

        val title = request.title
        val outputPath = createFilePath(title)

        log.debug("Output path: {}", outputPath)

        return aiStructureGenerator.generatePptStructure(title, request.content)
            .flatMap { slides ->
                val createFileMono = Mono.fromCallable {
                    XMLSlideShow().use { ppt ->
                        val defaultMaster = ppt.slideMasters[0]
                        val titleLayout = defaultMaster.getLayout(SlideLayout.TITLE)

                        ppt.createSlide(titleLayout).getPlaceholder(0).apply {
                            text = title
                            fillColor = Color(240, 240, 240)
                            textParagraphs[0].apply {
                                textAlign = TextParagraph.TextAlign.CENTER
                                textRuns[0].apply {
                                    fontSize = 44.toDouble()
                                    fontFamily = "맑은 고딕"
                                    isBold = true
                                    setFontColor(Color(44, 62, 80))
                                }
                            }
                        }

                        slides.forEach { slide ->
                            val slideTitle = slide["title"] ?: ""
                            val slideContent = slide["content"] ?: ""
                            val slideNotes = slide["notes"] ?: ""

                            val titleAndContentLayout = defaultMaster.getLayout(SlideLayout.TITLE_AND_CONTENT)
                            val slide = ppt.createSlide(titleAndContentLayout)
                            val titlePlaceholder = slide.getPlaceholder(0)

                            titlePlaceholder?.let { title ->
                                title.text = slideTitle
                                title.fillColor = Color(240, 240, 240)
                                title.textParagraphs.firstOrNull()
                                    ?.textRuns?.firstOrNull()
                                    ?.apply {
                                        fontSize = 32.0
                                        fontFamily = "맑은 고딕"
                                        isBold = true
                                        setFontColor(Color(44, 62, 80))
                                    }
                            }

                            val contentPlaceholder = slide.getPlaceholder(1)

                            contentPlaceholder?.let { content ->
                                content.clearText()
                                val lines = slideContent.split("\n")

                                lines.forEach { line ->
                                    val paragraph = content.addNewTextParagraph()
                                    val run = paragraph.addNewTextRun()

                                    run.setText(line)
                                    run.fontSize = 20.0
                                    run.fontFamily = "맑은 고딕"
                                }
                            }

                            if (slideNotes.isNotBlank()) {
                                val notes = ppt.getNotesSlide(slide)
                                val notesShape = notes.getPlaceholder(1)
                                notesShape.text = slideNotes
                            }
                        }

                        Files.createDirectories(outputPath.parent)
                        FileOutputStream(outputPath.toFile()).use { ppt.write(it) }

                        // TODO: 파일 생성 완료 시 문서 정보 DB 업데이트
                    }
                }.subscribeOn(Schedulers.boundedElastic())

                val saveDocumentMono = Mono.fromCallable {
                    Document.forProcessing(documentId, title)
                }.flatMap { documentManager.save(it) }

                // 파일 생성과 DB 저장 병렬 실행
                Mono.`when`(createFileMono, saveDocumentMono)
            }
            .onErrorResume { ex ->
                val errorMessage = ex.message
                log.error("Failed to generate PPT: $errorMessage", ex)
                val failedDocument = Document.forFailed(documentId, title, errorMessage ?: "FAIL")
                documentManager.save(failedDocument).then()
            }
    }

    private fun createFilePath(title: String): Path {
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return Paths.get("docs/ppt", "${titleName}_$timestamp.pptx")
    }
}