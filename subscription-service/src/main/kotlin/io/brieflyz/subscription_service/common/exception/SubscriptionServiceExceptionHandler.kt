package io.brieflyz.subscription_service.common.exception

import io.brieflyz.core.constants.ErrorCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorData
import io.brieflyz.core.utils.logger
import org.springframework.beans.BeansException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.sql.SQLException

@RestControllerAdvice
class SubscriptionServiceExceptionHandler {

    private val log = logger()

    companion object {
        private const val RUNTIME_ERROR_PREFIX = "[런타임 오류]"
        private const val INTERNAL_ERROR_PREFIX = "[서버 내부 오류]"
    }

    @ExceptionHandler(SubscriptionServiceException::class)
    fun handleSubscriptionServiceException(e: SubscriptionServiceException): ResponseEntity<ApiResponse<ErrorData>> {
        val errorCode = e.errorCode
        val apiResponse = ApiResponse.fail(errorCode, ErrorData.of(e))
        return ResponseEntity.status(errorCode.status).body(apiResponse)
            .also { log.warn("[구독 서비스 예외] ${e.localizedMessage}") }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ApiResponse<ErrorData> {
        val fieldErrors = ErrorData.FieldError.fromBindingResult(e.bindingResult)
        return ApiResponse.fail(ErrorCode.BAD_REQUEST, ErrorData.of(e, fieldErrors))
            .also { log.warn("[Validation 오류] ${e.localizedMessage}") }
    }

    @ExceptionHandler(NullPointerException::class, IndexOutOfBoundsException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleBasicRuntimeException(e: RuntimeException): ApiResponse<ErrorData> {
        val message = e.message ?: "Basic Runtime Exception"
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("$RUNTIME_ERROR_PREFIX ${e::class.simpleName} 발생: $message", e) }
    }

    @ExceptionHandler(
        IllegalStateException::class,
        SQLException::class,
        BeansException::class,
        IOException::class
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalException(e: Exception): ApiResponse<ErrorData> {
        val prefix = when (e) {
            is IllegalStateException -> "[서버 상태 오류]"
            is SQLException -> "[DB 오류]"
            is BeansException -> "[Spring Bean 오류]"
            is IOException -> "[입출력 오류]"
            else -> INTERNAL_ERROR_PREFIX
        }
        return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("$prefix ${e::class.simpleName} 발생: ${e.message}", e) }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleOtherExceptions(e: Exception): ApiResponse<ErrorData> =
        ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("$INTERNAL_ERROR_PREFIX 처리되지 않은 예외 발생: ${e.message}", e) }

    @ExceptionHandler(OutOfMemoryError::class, StackOverflowError::class)
    fun handleErrors(e: Error) {
        log.error("[심각] ${e::class.simpleName} 발생: ${e.message}", e)
    }
}