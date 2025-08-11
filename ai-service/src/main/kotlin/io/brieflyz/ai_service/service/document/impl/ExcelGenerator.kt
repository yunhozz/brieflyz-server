package io.brieflyz.ai_service.service.document.impl

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.ai_service.model.entity.Document
import io.brieflyz.ai_service.service.ai.AiStructureGeneratorFactory
import io.brieflyz.ai_service.service.document.DocumentGenerator
import io.brieflyz.ai_service.service.document.DocumentManager
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.kafka.DocumentRequestMessage
import io.brieflyz.core.utils.logger
import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.io.path.name

@Component
class ExcelGenerator(
    private val aiStructureGeneratorFactory: AiStructureGeneratorFactory,
    private val documentManager: DocumentManager
) : DocumentGenerator {

    private val log = logger()

    override fun getDocumentType() = DocumentType.EXCEL

    override fun generateDocument(aiProvider: AiProvider, request: DocumentRequestMessage): Mono<DocumentResponse> {
        val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(aiProvider)
        val documentId = UUID.randomUUID().toString()

        val title = request.title
        val filePath = createFilePath(title)

        log.debug("Excel file path: {}", filePath)

        return aiStructureGenerator.generateExcelStructure(title, request.content)
            .flatMap { sheetData ->
                val createFileMono = Mono.fromCallable {
                    XSSFWorkbook().use { workbook ->
                        createExcel(workbook, sheetData)

                        Files.createDirectories(filePath.parent)
                        FileOutputStream(filePath.toFile()).use { workbook.write(it) }

                        log.info("Create excel file completed. File path: ${filePath.name}")
                    }
                }.subscribeOn(Schedulers.boundedElastic())
                    .then(
                        Mono.defer {
                            val fileName = filePath.fileName.toString()
                            val fileUrl = filePath.toUri().toURL().toString()
                            val downloadUrl = filePath.toUri().toURL().toString()
                            documentManager.updateStatus(documentId, fileName, fileUrl, downloadUrl)
                                .doOnSuccess { log.info("Excel document update finish. ID: $documentId") }
                        }
                    )

                val saveDocumentMono = Mono.fromCallable {
                    Document.forProcessing(documentId, title)
                }.flatMap { documentManager.save(it) }.cache()

                Mono.`when`(createFileMono, saveDocumentMono) // 파일 생성과 DB 저장 병렬 실행
                    .then(saveDocumentMono)
            }
            .onErrorResume { ex ->
                val errorMessage = ex.message
                log.error("Failed to generate EXCEL: $errorMessage", ex)
                val failedDocument = Document.forFailed(documentId, title, errorMessage ?: "FAIL")
                documentManager.save(failedDocument)
            }
    }

    private fun createExcel(workbook: XSSFWorkbook, sheetData: Map<String, List<List<String>>>) {
        fun createHeaderStyle(): XSSFCellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setFont(
                workbook.createFont().apply {
                    fontName = "맑은 고딕"
                    fontHeightInPoints = 11.toShort()
                    bold = true
                }
            )
        }

        fun createDefaultCellStyle(): XSSFCellStyle = workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setFont(
                workbook.createFont().apply {
                    fontName = "맑은 고딕"
                    fontHeightInPoints = 10.toShort()
                    bold = true
                }
            )
        }

        val headerStyle = createHeaderStyle()
        val defaultCellStyle = createDefaultCellStyle()

        sheetData.forEach { (sheetName, rows) ->
            val sheet = workbook.createSheet(sheetName)

            rows.forEachIndexed { i, cells ->
                val row = sheet.createRow(i)

                cells.forEachIndexed { j, cellValue ->
                    val cell = row.createCell(j)
                    cell.setCellValue(cellValue)
                    cell.cellStyle = if (i == 0) headerStyle else defaultCellStyle
                }
            }

            val columnCount = rows.firstOrNull()?.size ?: 0

            for (i in 0 until columnCount) {
                sheet.autoSizeColumn(i)
                val currentWidth = sheet.getColumnWidth(i)
                when {
                    currentWidth < 3000 -> sheet.setColumnWidth(i, 3000)
                    currentWidth > 15000 -> sheet.setColumnWidth(i, 15000)
                }
            }
        }
    }

    private fun createFilePath(title: String): Path {
        val titleName = title.replace(Regex("[^a-zA-Z0-9가-힣]"), "_")
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return Paths.get("docs/excel", "${titleName}_$timestamp.xlsx")
    }
}