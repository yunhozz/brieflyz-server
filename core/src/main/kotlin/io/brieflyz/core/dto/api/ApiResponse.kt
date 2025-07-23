package io.brieflyz.core.dto.api

import com.fasterxml.jackson.annotation.JsonInclude
import io.brieflyz.core.constants.ApiStatus
import io.brieflyz.core.constants.ErrorStatus
import io.brieflyz.core.constants.SuccessStatus
import org.springframework.validation.BindingResult
import java.time.LocalDateTime

data class ApiResponse<T> private constructor(
    val header: ApiHeader,
    val body: ApiBody<T>
) {
    companion object {
        fun <T : Any> success(successStatus: SuccessStatus, data: T? = null): ApiResponse<T> {
            val header = ApiHeader.of(successStatus, true)
            val body = ApiBody.of(successStatus.message, data)
            return ApiResponse(header, body)
        }

        fun fail(errorStatus: ErrorStatus, errorData: ErrorData): ApiResponse<ErrorData> {
            val header = ApiHeader.of(errorStatus, false)
            val body = ApiBody.of(errorStatus.message, errorData)
            return ApiResponse(header, body)
        }
    }

    data class ApiHeader private constructor(
        val status: ApiStatus,
        val success: Boolean
    ) {
        companion object {
            internal fun of(status: ApiStatus, success: Boolean) = ApiHeader(status, success)
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
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorData private constructor(
    val timestamp: LocalDateTime,
    val exception: String?,
    val fieldErrors: List<FieldError>?
) {
    companion object {
        fun of(ex: Exception, fieldErrors: List<FieldError>? = null) =
            ErrorData(LocalDateTime.now(), ex::class.qualifiedName, fieldErrors)
    }

    data class FieldError private constructor(
        val field: String,
        val value: String?,
        val reason: String?
    ) {
        companion object {
            fun fromBindingResult(result: BindingResult): List<FieldError> =
                result.fieldErrors.map { err ->
                    FieldError(err.field, err.rejectedValue?.toString(), err.defaultMessage)
                }
        }
    }
}