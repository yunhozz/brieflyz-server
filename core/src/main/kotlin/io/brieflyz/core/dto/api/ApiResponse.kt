package io.brieflyz.core.dto.api

import org.springframework.validation.BindingResult
import java.time.LocalDateTime

data class ApiResponse<T> private constructor(
    val header: ApiHeader,
    val message: String,
    val data: T?
) {
    companion object {
        fun <T> success(successCode: SuccessCode, data: T? = null): ApiResponse<T> {
            val header = ApiHeader(successCode, true)
            return ApiResponse(header, successCode.message, data)
        }

        fun fail(errorCode: ErrorCode, errorData: ErrorData): ApiResponse<ErrorData> {
            val header = ApiHeader(errorCode, false)
            return ApiResponse(header, errorCode.message, errorData)
        }
    }

    data class ApiHeader(val code: ApiResponseCode, val success: Boolean)

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