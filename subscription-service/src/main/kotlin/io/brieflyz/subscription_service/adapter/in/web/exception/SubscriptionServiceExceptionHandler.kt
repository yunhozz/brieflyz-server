package io.brieflyz.subscription_service.adapter.`in`.web.exception

import io.brieflyz.core.constants.ErrorStatus
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorData
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.exception.SubscriptionServiceException
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

    @ExceptionHandler(SubscriptionServiceException::class)
    fun handleSubscriptionServiceException(e: SubscriptionServiceException): ResponseEntity<ApiResponse<ErrorData>> {
        val status = e.status
        val apiResponse = ApiResponse.Companion.fail(status, ErrorData.Companion.of(e))
        return ResponseEntity.status(status.statusCode).body(apiResponse)
            .also { log.warn("[구독 서비스 예외] ${e.localizedMessage}") }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ApiResponse<ErrorData> {
        val fieldErrors = ErrorData.FieldError.fromBindingResult(e.bindingResult)
        return ApiResponse.fail(ErrorStatus.BAD_REQUEST, ErrorData.of(e, fieldErrors))
            .also { log.warn("[Validation 오류] ${e.localizedMessage}") }
    }

    @ExceptionHandler(
        NullPointerException::class,
        IndexOutOfBoundsException::class,
        IllegalStateException::class
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleRuntimeExceptions(e: RuntimeException): ApiResponse<ErrorData> {
        val prefix = when (e) {
            is NullPointerException, is IndexOutOfBoundsException -> "[런타임 오류]"
            is IllegalStateException -> "[서버 상태 오류]"
            else -> "[런타임 내부 오류]"
        }

        return ApiResponse.fail(ErrorStatus.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("$prefix ${e::class.simpleName} 발생: ${e.message}", e) }
    }

    @ExceptionHandler(
        SQLException::class,
        BeansException::class,
        IOException::class
    )
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleCheckedExceptions(e: Exception): ApiResponse<ErrorData> {
        val prefix = when (e) {
            is SQLException -> "[DB 오류]"
            is BeansException -> "[Spring Bean 오류]"
            is IOException -> "[입출력 오류]"
            else -> "[서버 내부 오류]"
        }

        return ApiResponse.fail(ErrorStatus.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("$prefix ${e::class.simpleName} 발생: ${e.message}", e) }
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleAllExceptions(e: Exception): ApiResponse<ErrorData> =
        ApiResponse.fail(ErrorStatus.INTERNAL_SERVER_ERROR, ErrorData.of(e))
            .also { log.error("[서버 내부 오류] 처리되지 않은 예외 발생: ${e.message}", e) }

    @ExceptionHandler(OutOfMemoryError::class, StackOverflowError::class)
    fun handleResourceError(e: Error) {
        log.error("[리소스 오류] ${e::class.simpleName} 발생: ${e.message}", e)
    }

    @ExceptionHandler(Error::class)
    fun handleAllErrors(e: Error) {
        log.error("[심각] ${e::class.simpleName} 발생: ${e.message}", e)
    }
}