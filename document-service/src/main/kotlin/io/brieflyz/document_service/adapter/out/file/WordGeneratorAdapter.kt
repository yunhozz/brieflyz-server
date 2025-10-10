package io.brieflyz.document_service.adapter.out.file

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.document.Section
import io.brieflyz.core.dto.document.WordStructure
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
import io.brieflyz.document_service.application.port.`in`.UpdateDocumentStatusUseCase
import io.brieflyz.document_service.application.port.`in`.UpdateFileInfoUseCase
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.config.DocumentServiceProperties
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.FileOutputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.name

@Component
class WordGeneratorAdapter(
    private val updateDocumentStatusUseCase: UpdateDocumentStatusUseCase,
    private val updateFileInfoUseCase: UpdateFileInfoUseCase,
    private val props: DocumentServiceProperties,
    private val objectMapper: ObjectMapper
) : AbstractDocumentGeneratorAdapter(updateDocumentStatusUseCase, props) {

    private val log = logger()

    override fun getDocumentType() = DocumentType.WORD

    override fun generateDocument(documentId: String, title: String, structure: Any?): Mono<Void> {
        val filePath = createFilePath(title)
        val wordStructure = objectMapper.convertValue(
            structure,
            object : TypeReference<WordStructure>() {}
        )

        log.debug("Word file path={}", filePath)

        return Mono.justOrEmpty(wordStructure)
            .flatMap { structure ->
                Mono.fromCallable {
                    XWPFDocument().use { document ->
                        createWord(document, structure)
                        Files.createDirectories(filePath.parent)
                        FileOutputStream(filePath.toFile()).use { document.write(it) }
                        log.info("Create word file completed. File path=${filePath.name}")
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
                            val downloadUrl = props.file.downloadUrl

                            val command = UpdateFileInfoCommand(
                                documentId,
                                fileName,
                                fileUrl,
                                downloadUrl = "$downloadUrl/word"
                            )

                            updateFileInfoUseCase.update(command)
                                .doOnSuccess { log.info("Word document update finish. ID=$documentId") }
                        }
                    )
                    .doOnError { ex -> log.error("Background task failed", ex) }
                    .subscribe()

                val command = UpdateDocumentCommand(documentId, DocumentStatus.PROCESSING)
                updateDocumentStatusUseCase.update(command)
            }
    }

    private fun createWord(document: XWPFDocument, structure: WordStructure) {
        fun appendSection(document: XWPFDocument, section: Section, level: Int = 1) {
            val headingParagraph = document.createParagraph().apply {
                alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.LEFT
            }

            headingParagraph.createRun().apply {
                setText(section.heading)
                fontSize = when (level) {
                    1 -> 16
                    2 -> 14
                    else -> 12
                }
                isBold = true
                addBreak()
            }

            document.createParagraph().createRun().apply {
                setText(section.content)
                fontSize = 12
                addBreak()
            }

            section.subSections.forEach { sub ->
                appendSection(document, sub, level + 1)
            }
        }

        val titleParagraph = document.createParagraph().apply {
            alignment = org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER
        }

        titleParagraph.createRun().apply {
            setText(structure.title)
            fontSize = 20
            isBold = true
            addBreak()
        }

        document.createParagraph().createRun().apply {
            setText(structure.description)
            fontSize = 12
            addBreak()
        }

        structure.sections.forEach { section ->
            appendSection(document, section)
        }

        document.createParagraph().createRun().apply {
            addBreak()
            setText("요약")
            isBold = true
            fontSize = 14
        }

        document.createParagraph().createRun().apply {
            setText(structure.summary)
            fontSize = 12
        }
    }
}