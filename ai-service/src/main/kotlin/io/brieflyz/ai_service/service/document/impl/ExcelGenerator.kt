package io.brieflyz.ai_service.service.document.impl

import io.brieflyz.ai_service.common.enums.AiProvider
import io.brieflyz.ai_service.model.dto.DocumentResponse
import io.brieflyz.ai_service.service.ai.AiStructureGeneratorFactory
import io.brieflyz.ai_service.service.document.DocumentGenerator
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
import java.util.UUID

@Component
class ExcelGenerator(
    private val aiStructureGeneratorFactory: AiStructureGeneratorFactory
) : DocumentGenerator {

    private val log = logger()

    override fun getDocumentType() = DocumentType.EXCEL

    override fun generateDocument(aiProvider: AiProvider, request: DocumentRequestMessage): Mono<DocumentResponse> {
        val aiStructureGenerator = aiStructureGeneratorFactory.createByProvider(aiProvider)
        val documentId = UUID.randomUUID().toString()

        val title = request.title
        val outputPath = createFilePath(title, "temp/excel")

        log.debug("Output path: {}", outputPath)

        return aiStructureGenerator.generateExcelStructure(title, request.content)
            .flatMap { sheetData ->
                Mono.fromCallable {
                    XSSFWorkbook().use { workbook ->
                        val headerStyle = createHeaderStyle(workbook)
                        val defaultCellStyle = createDefaultCellStyle(workbook)

                        sheetData.forEach { (sheetName, rows) ->
                            val sheet = workbook.createSheet(sheetName)

                            for ((i, cells) in rows.withIndex()) {
                                val row = sheet.createRow(i)

                                for ((j, cellValue) in cells.withIndex()) {
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

                        Files.createDirectories(outputPath.parent)

                        FileOutputStream(outputPath.toFile()).use { fos ->
                            workbook.write(fos)
                        }
                    }
                }.subscribeOn(Schedulers.boundedElastic())
            }.flatMap {
                // TODO: 문서 상태 DB 저장
                Mono.just(DocumentResponse.forProcessing(documentId, title))
                    .doOnNext { log.info("Processing document info: $it") }
            }
    }

    private fun createHeaderStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val xssfFont = workbook.createFont().apply {
            fontName = "맑은 고딕"
            fontHeightInPoints = 11.toShort()
            bold = true
        }

        return workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setFont(xssfFont)
        }
    }

    private fun createDefaultCellStyle(workbook: XSSFWorkbook): XSSFCellStyle {
        val xssfFont = workbook.createFont().apply {
            fontName = "맑은 고딕"
            fontHeightInPoints = 10.toShort()
            bold = true
        }

        return workbook.createCellStyle().apply {
            alignment = HorizontalAlignment.LEFT
            verticalAlignment = VerticalAlignment.CENTER
            borderTop = BorderStyle.THIN
            borderBottom = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            setFont(xssfFont)
        }
    }
}