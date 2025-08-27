package io.brieflyz.document_service.model.dto

import org.springframework.core.io.Resource
import org.springframework.http.MediaType

data class DocumentResourceResponse(
    val fileName: String,
    val resource: Resource,
    val mediaType: MediaType
)