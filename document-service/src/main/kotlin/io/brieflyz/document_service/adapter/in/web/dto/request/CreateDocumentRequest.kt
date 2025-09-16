package io.brieflyz.document_service.adapter.`in`.web.dto.request

import io.brieflyz.core.constants.AiProvider
import io.brieflyz.core.constants.DocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateDocumentRequest(
    @field:NotNull(message = "AI 모델을 선택해주세요.")
    val aiProvider: AiProvider,

    @field:NotBlank(message = "제목을 입력해주세요.")
    val title: String,

    @field:NotBlank(message = "요청하실 내용을 입력해주세요.")
    val content: String,

    @field:NotNull(message = "문서 타입은 필수입니다.")
    val documentType: DocumentType,

    val templateName: String?,
    val sections: List<String>?,
    val additionalOptions: Map<String, Any>?
)