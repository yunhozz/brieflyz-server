package io.brieflyz.document_service.adapter.out.file

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.document.PowerPointStructure
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
import io.brieflyz.document_service.application.port.`in`.UpdateDocumentStatusUseCase
import io.brieflyz.document_service.application.port.`in`.UpdateFileInfoUseCase
import io.brieflyz.document_service.application.port.out.DocumentGeneratorPort
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.config.DocumentServiceProperties
import org.apache.poi.sl.usermodel.PictureData
import org.apache.poi.sl.usermodel.TextParagraph
import org.apache.poi.xslf.usermodel.SlideLayout
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.awt.Color
import java.awt.Rectangle
import java.io.FileOutputStream
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name

@Component
class PowerPointGeneratorAdapter(
    private val updateDocumentStatusUseCase: UpdateDocumentStatusUseCase,
    private val updateFileInfoUseCase: UpdateFileInfoUseCase,
    private val documentServiceProperties: DocumentServiceProperties,
    private val objectMapper: ObjectMapper
) : DocumentGeneratorPort {

    private val log = logger()

    override fun getDocumentType() = DocumentType.POWERPOINT

    override fun generateDocument(documentId: String, title: String, structure: Any?): Mono<Void> {
        val filePath = createFilePath(title)
        val pptStructure = objectMapper.convertValue(
            structure,
            object : TypeReference<PowerPointStructure>() {}
        )

        log.debug("PPT file path={}", filePath)

        return Mono.justOrEmpty(pptStructure)
            .flatMap { slides ->
                Mono.fromCallable {
                    XMLSlideShow().use { ppt ->
                        createPowerPoint(ppt, title, slides)
                        Files.createDirectories(filePath.parent)
                        FileOutputStream(filePath.toFile()).use { ppt.write(it) }
                        log.info("Create PPT file completed. File path=${filePath.name}")
                    }
                }.subscribeOn(Schedulers.boundedElastic())
                    .onErrorResume { ex ->
                        val errorMessage = "${ex::class.qualifiedName} ${ex.localizedMessage}"
                        updateDocumentFailed(documentId, errorMessage).then(Mono.error(ex))
                    }
                    .then(
                        Mono.defer {
                            val fileName = filePath.fileName.toString()
                            val fileUrl = URLDecoder.decode(filePath.toUri().toURL().toString(), StandardCharsets.UTF_8)
                            val downloadUrl = documentServiceProperties.file.downloadUrl

                            val command = UpdateFileInfoCommand(
                                documentId,
                                fileName,
                                fileUrl,
                                downloadUrl = "$downloadUrl/ppt"
                            )

                            updateFileInfoUseCase.update(command)
                                .doOnSuccess { log.info("PPT document update finish. ID=$documentId") }
                        }
                    )
                    .doOnError { ex -> log.error("Background task failed", ex) }
                    .subscribe()

                val command = UpdateDocumentCommand(documentId, DocumentStatus.PROCESSING)
                updateDocumentStatusUseCase.update(command)
            }
    }

    override fun updateDocumentFailed(documentId: String, errMsg: String): Mono<Void> {
        log.warn("Failed to generate PPT. Reason : $errMsg")
        val command = UpdateDocumentCommand(documentId, DocumentStatus.FAILED, errMsg)
        return updateDocumentStatusUseCase.update(command).then()
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
            val imagePath = slide["image"] ?: ""

            val titleAndContentLayout = defaultMaster.getLayout(SlideLayout.TITLE_AND_CONTENT)
            val slide = ppt.createSlide(titleAndContentLayout)

            val title = slide.getPlaceholder(0)
            val content = slide.getPlaceholder(1)

            title?.apply {
                text = slideTitle
                fillColor = Color(240, 240, 240)
                textParagraphs.firstOrNull()
                    ?.textRuns?.firstOrNull()
                    ?.apply {
                        fontSize = 32.0
                        fontFamily = "맑은 고딕"
                        isBold = true
                        setFontColor(Color(44, 62, 80))
                    }
            }

            content?.apply {
                clearText()
                val lines = slideContent.split("\n")

                lines.forEach { line ->
                    val paragraph = addNewTextParagraph()
                    val run = paragraph.addNewTextRun()

                    run.setText(line)
                    run.fontSize = 20.0
                    run.fontFamily = "맑은 고딕"
                }
            }

            if (imagePath.isNotBlank()) {
                val imageBytes = imagePath.takeIf { it.startsWith("http") }?.let { path ->
                    URI(path).toURL().openStream().use { it.readAllBytes() }
                } ?: Files.readAllBytes(Paths.get(imagePath))

                val pictureData = ppt.addPicture(imageBytes, PictureData.PictureType.PNG)
                val pictureShape = slide.createPicture(pictureData)

                val pageSize = ppt.pageSize
                val width = pageSize.width / 2
                val height = pageSize.height / 2

                pictureShape.anchor = Rectangle(width / 2, height / 2, width, height)
            }

            if (slideNotes.isNotBlank()) {
                val notes = ppt.getNotesSlide(slide)
                val notesShape = notes.getPlaceholder(1)
                notesShape.text = slideNotes
            }
        }
    }

    private fun createFilePath(title: String): Path {
        val filePath = documentServiceProperties.file.filePath
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = System.nanoTime()
        return Paths.get("$filePath/ppt", "${titleName}_$timestamp.pptx")
    }
}