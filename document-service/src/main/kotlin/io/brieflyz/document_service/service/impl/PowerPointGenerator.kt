package io.brieflyz.document_service.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.config.DocumentServiceProperties
import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.model.entity.Document
import io.brieflyz.document_service.service.DocumentGenerator
import io.brieflyz.document_service.service.DocumentManager
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
import kotlin.io.path.name

@Component
class PowerPointGenerator(
    private val documentServiceProperties: DocumentServiceProperties,
    private val documentManager: DocumentManager,
    private val objectMapper: ObjectMapper
) : DocumentGenerator {

    private val log = logger()

    override fun getDocumentType() = DocumentType.POWERPOINT

    override fun generateDocument(title: String, structure: Any): Mono<DocumentResponse> {
        val documentId = UUID.randomUUID().toString()
        val filePath = createFilePath(title)
        val pptStructure = objectMapper.convertValue(structure, object : TypeReference<List<Map<String, String>>>() {})

        log.debug("PPT file path: {}", filePath)

        return Mono.justOrEmpty(pptStructure)
            .flatMap { slides ->
                Mono.fromCallable {
                    XMLSlideShow().use { ppt ->
                        createPowerPoint(ppt, title, slides)
                        Files.createDirectories(filePath.parent)
                        FileOutputStream(filePath.toFile()).use { ppt.write(it) }
                        log.info("Create PPT file completed. File path: ${filePath.name}")
                    }
                }.subscribeOn(Schedulers.boundedElastic())
                    .then(
                        Mono.defer {
                            val fileName = filePath.fileName.toString()
                            val fileUrl = filePath.toUri().toURL().toString()
                            val downloadUrl = documentServiceProperties.file?.downloadUrl

                            documentManager.updateStatus(documentId, fileName, fileUrl, "$downloadUrl/ppt")
                                .doOnSuccess { log.info("PPT document update finish. ID: $documentId") }
                        }
                    )
                    .doOnError { ex -> log.error("Background task failed: ${ex.message}", ex) }
                    .subscribe()

                Mono.just(Document.forProcessing(documentId, title))
                    .flatMap { documentManager.save(it) }
            }
            .onErrorResume { ex ->
                val errorMessage = ex.message
                log.error("Failed to generate PPT: $errorMessage", ex)
                val failedDocument = Document.forFailed(documentId, title, errorMessage ?: "FAIL")
                documentManager.save(failedDocument)
            }
    }

    private fun createPowerPoint(ppt: XMLSlideShow, title: String, slides: List<Map<String, String>>) {
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
    }

    private fun createFilePath(title: String): Path {
        val filePath = documentServiceProperties.file?.filePath
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return Paths.get("$filePath/ppt", "${titleName}_$timestamp.pptx")
    }
}