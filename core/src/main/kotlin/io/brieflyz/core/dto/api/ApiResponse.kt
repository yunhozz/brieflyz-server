package io.brieflyz.core.dto.api

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.BindingResult
import java.time.LocalDateTime

data class ApiResponse<T> private constructor(
    val header: ApiHeader,
    val body: ApiBody<T>
) {
    companion object {
        fun <T> success(successCode: SuccessCode, data: T? = null): ApiResponse<T> {
            val header = ApiHeader(successCode, true)
            val body = ApiBody(successCode.message, data)
            return ApiResponse(header, body)
        }

        fun fail(errorCode: ErrorCode, errorData: ErrorData): ApiResponse<ErrorData> {
            val header = ApiHeader(errorCode, false)
            val body = ApiBody(errorCode.message, errorData)
            return ApiResponse(header, body)
        }
    }

    data class ApiHeader(val code: ApiResponseCode, val success: Boolean)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ApiBody<T>(val message: String, val data: T?)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ErrorData private constructor(
        val timestamp: LocalDateTime,
        val exception: String,
        val fieldErrors: List<FieldError>?
    ) {
        companion object {
            fun of(ex: String, fieldErrors: List<FieldError>? = null) =
                ErrorData(LocalDateTime.now(), ex, fieldErrors)
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