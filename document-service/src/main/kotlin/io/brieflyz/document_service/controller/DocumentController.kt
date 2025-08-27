package io.brieflyz.document_service.controller

import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.constants.SuccessStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.document_service.model.dto.DocumentCreateRequest
import io.brieflyz.document_service.model.dto.DocumentResponse
import io.brieflyz.document_service.service.DocumentService
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

@RestController
@RequestMapping("/api/documents")
class DocumentController(
    private val documentService: DocumentService
) {
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDocument(
        @JwtSubject username: String,
        @RequestBody @Valid request: DocumentCreateRequest
    ): Mono<ApiResponse<DocumentResponse>> =
        documentService.createDocumentWithAi(username, request)
            .map { document ->
                ApiResponse.success(SuccessStatus.DOCUMENT_CREATION_REQUEST_SUCCESS, document)
            }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getDocumentList(@JwtSubject username: String): Mono<ApiResponse<List<DocumentResponse>>> =
        documentService.getDocumentListByUsername(username)
            .map { documentList ->
                ApiResponse.success(SuccessStatus.DOCUMENT_LIST_READ_SUCCESS, documentList)
            }

    @GetMapping("/download/{documentId}")
    @ResponseStatus(HttpStatus.OK)
    fun downloadDocument(@PathVariable documentId: String): Mono<ResponseEntity<Resource>> =
        documentService.createDocumentResource(documentId)
            .map { response ->
                val headers = HttpHeaders().apply {
                    val fileName = response.fileName
                    val encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''$encodedFileName")
                }

                ResponseEntity.ok()
                    .headers(headers)
                    .contentType(response.mediaType)
                    .body(response.resource)
            }
}