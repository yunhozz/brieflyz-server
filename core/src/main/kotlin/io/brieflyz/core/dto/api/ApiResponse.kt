package io.brieflyz.core.dto.api

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.BindingResult
import java.time.LocalDateTime

data class ApiResponse<T> private constructor(
    private val header: ApiHeader,
    private val body: ApiBody<T>
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
        private val code: ApiResponseCode,
        private val success: Boolean
    ) {
        companion object {
            internal fun of(code: ApiResponseCode, success: Boolean) = ApiHeader(code, success)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ApiBody<T> private constructor(
        private val message: String,
        private val data: T?
    ) {
        companion object {
            internal fun <T> of(message: String, data: T? = null) = ApiBody(message, data)
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ErrorData private constructor(
        private val timestamp: LocalDateTime,
        private val exception: String,
        private val fieldErrors: List<FieldError>?
    ) {
        companion object {
            internal fun of(ex: String, fieldErrors: List<FieldError>? = null) =
                ErrorData(LocalDateTime.now(), ex, fieldErrors)
        }

        data class FieldError private constructor(
            private val field: String,
            private val value: String?,
            private val reason: String?
        ) {
            companion object {
                internal fun of(result: BindingResult): List<FieldError> =
                    result.fieldErrors.map { err ->
                        FieldError(err.field, err.rejectedValue?.toString(), err.defaultMessage)
                    }
            }
        }
    }
}