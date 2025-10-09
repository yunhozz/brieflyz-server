package io.brieflyz.document_service.adapter.out.file

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.DocumentType
import io.brieflyz.core.dto.document.ExcelStructure
import io.brieflyz.core.utils.logger
import io.brieflyz.document_service.application.dto.command.UpdateDocumentCommand
import io.brieflyz.document_service.application.dto.command.UpdateFileInfoCommand
import io.brieflyz.document_service.application.port.`in`.UpdateDocumentStatusUseCase
import io.brieflyz.document_service.application.port.`in`.UpdateFileInfoUseCase
import io.brieflyz.document_service.common.enums.DocumentStatus
import io.brieflyz.document_service.config.DocumentServiceProperties
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.io.path.name

@Component
class ExcelGeneratorAdapter(
    private val updateDocumentStatusUseCase: UpdateDocumentStatusUseCase,
    private val updateFileInfoUseCase: UpdateFileInfoUseCase,
    private val props: DocumentServiceProperties,
    private val objectMapper: ObjectMapper
) : AbstractDocumentGeneratorAdapter(updateDocumentStatusUseCase, props) {

    private val log = logger()

    override fun getDocumentType() = DocumentType.EXCEL

    override fun generateDocument(documentId: String, title: String, structure: Any?): Mono<Void> {
        val filePath = createFilePath(title)
        val excelStructure = objectMapper.convertValue(
            structure,
            object : TypeReference<ExcelStructure>() {}
        )

        log.debug("Excel file path={}", filePath)

        return Mono.justOrEmpty(excelStructure)
            .flatMap { sheetData ->
                Mono.fromCallable {
                    XSSFWorkbook().use { workbook ->
                        createExcel(workbook, sheetData)
                        Files.createDirectories(filePath.parent)
                        FileOutputStream(filePath.toFile()).use { workbook.write(it) }
                        log.info("Create excel file completed. File path=${filePath.name}")
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
                                downloadUrl = "$downloadUrl/excel"
                            )

                            updateFileInfoUseCase.update(command)
                                .doOnSuccess { log.info("Excel document update finish. ID=$documentId") }
                        }
                    )
                    .doOnError { ex -> log.error("Background task failed", ex) }
                    .subscribe()

                val command = UpdateDocumentCommand(documentId, DocumentStatus.PROCESSING)
                updateDocumentStatusUseCase.update(command)
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
}