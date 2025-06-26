package io.brieflyz.core.dto.api

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.BindingResult
import java.time.LocalDateTime

data class ApiResponse<T> private constructor(
    val header: ApiHeader,
    val body: ApiBody<T>
) {
    companion object {
        fun <T : Any> success(successCode: SuccessCode, data: T? = null): ApiResponse<T> {
            val header = ApiHeader.of(successCode, true)
            val body = ApiBody.of(successCode.message, data)
            return ApiResponse(header, body)
        }

        fun fail(errorCode: ErrorCode, errorData: ErrorData): ApiResponse<ErrorData> {
            val header = ApiHeader.of(errorCode, false)
            val body = ApiBody.of(errorCode.message, errorData)
            return ApiResponse(header, body)
        }
    }

    data class ApiHeader private constructor(
        val code: ApiResponseCode,
        val success: Boolean
    ) {
        companion object {
            internal fun of(code: ApiResponseCode, success: Boolean) = ApiHeader(code, success)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ApiBody<T> private constructor(
        val message: String,
        val data: T?
    ) {
        companion object {
            internal fun <T> of(message: String, data: T? = null) = ApiBody(message, data)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ErrorData private constructor(
        val timestamp: LocalDateTime,
        val exception: String,
        val fieldErrors: List<FieldError>?
    ) {
        companion object {
            fun of(ex: String, fieldErrors: List<FieldError>? = null) = ErrorData(LocalDateTime.now(), ex, fieldErrors)
        }

        data class FieldError private constructor(
            val field: String,
            val value: String?,
            val reason: String?
        ) {
            companion object {
                fun of(result: BindingResult): List<FieldError> =
                    result.fieldErrors.map { err ->
                        FieldError(err.field, err.rejectedValue?.toString(), err.defaultMessage)
                    }
            }
        }
    }
}