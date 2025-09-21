package io.brieflyz.document_service.adapter.`in`.web.controller

import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.document_service.adapter.`in`.web.dto.request.CreateDocumentRequest
import io.brieflyz.document_service.adapter.`in`.web.dto.response.DocumentResponse
import io.brieflyz.document_service.application.dto.command.CreateDocumentCommand
import io.brieflyz.document_service.application.dto.result.DocumentResult
import io.brieflyz.document_service.application.port.`in`.CreateDocumentResourceUseCase
import io.brieflyz.document_service.application.port.`in`.CreateDocumentWithAiUseCase
import io.brieflyz.document_service.application.port.`in`.FindDocumentListUseCase
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val createDocumentWithAiUseCase: CreateDocumentWithAiUseCase,
    private val findDocumentListUseCase: FindDocumentListUseCase,
    private val createDocumentResourceUseCase: CreateDocumentResourceUseCase
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @JwtSubject username: String,
        @RequestBody @Valid request: CreateDocumentRequest
    ): Mono<ApiResponse<DocumentResponse>> =
        createDocumentWithAiUseCase.create(request.toCommand(username))
            .map { result ->
                ApiResponse.success(SuccessStatus.DOCUMENT_CREATION_REQUEST_SUCCESS, result.toResponse())
            }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDocumentList(@JwtSubject username: String): Mono<ApiResponse<List<DocumentResponse>>> =
        findDocumentListUseCase.findDocumentListByUsername(username)
            .map { documentResults ->
                val responses = documentResults.map { it.toResponse() }
                ApiResponse.success(SuccessStatus.DOCUMENT_LIST_READ_SUCCESS, responses)
            }

    @GetMapping("/download/{documentId}")
    @ResponseStatus(HttpStatus.OK)
    fun downloadDocument(@PathVariable documentId: String): Mono<ResponseEntity<Resource>> =
        createDocumentResourceUseCase.create(documentId)
            .map { result ->
                val headers = HttpHeaders().apply {
                    val fileName = result.fileName
                    val encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFileName")
                }

                ResponseEntity.ok()
                    .headers(headers)
                    .contentType(result.mediaType)
                    .body(result.resource)
            }
}

private fun CreateDocumentRequest.toCommand(username: String) = CreateDocumentCommand(
    username,
    aiProvider = this.aiProvider,
    title = this.title,
    content = this.content,
    documentType = this.documentType,
    templateName = this.templateName,
    sections = this.sections,
    additionalOptions = this.additionalOptions
)

private fun DocumentResult.toResponse() = DocumentResponse(
    documentId = this.documentId,
    title = this.title,
    fileName = this.fileName,
    fileUrl = this.fileUrl,
    downloadUrl = this.downloadUrl,
    status = this.status,
    errorMessage = this.errorMessage,
    createdAt = LocalDateTime.now(),
    updatedAt = LocalDateTime.now()
)