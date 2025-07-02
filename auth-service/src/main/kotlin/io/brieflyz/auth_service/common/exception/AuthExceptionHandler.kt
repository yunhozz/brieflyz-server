package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.constants.ErrorCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorData
import io.brieflyz.core.utils.logger
import org.springframework.beans.BeansException
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.sql.SQLException

@RestControllerAdvice
class AuthExceptionHandler {

    private val log = logger()

    companion object {
        private val INTERNAL_SERVER_ERROR_CODE = HttpStatusCode.valueOf(ErrorCode.INTERNAL_SERVER_ERROR.status)
        private const val RUNTIME_ERROR = "[런타임 오류]"
        private const val INTERNAL_ERROR = "[서버 내부 오류]"
    }

    @ExceptionHandler(AuthServiceException::class)
    fun handleAuthServiceException(e: AuthServiceException): ResponseEntity<ApiResponse<ErrorData>> {
        val errorCode = e.errorCode
        val apiResponse = ApiResponse.fail(errorCode, ErrorData.of(e))
        return ResponseEntity.status(errorCode.status)
            .body(apiResponse)
            .also { log.warn("[인증 서비스 예외] ${e.localizedMessage}") }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<ErrorData>> {
        val errorCode = ErrorCode.BAD_REQUEST
        val errorData = ErrorData.of(e, ErrorData.FieldError.fromBindingResult(e.bindingResult))
        val apiResponse = ApiResponse.fail(errorCode, errorData)
        return ResponseEntity.status(errorCode.status)
            .body(apiResponse)
            .also { log.warn("[Validation 오류] ${e.localizedMessage}") }
    }

    @ExceptionHandler(NullPointerException::class, IndexOutOfBoundsException::class)
    fun handleRuntimeException(e: RuntimeException): ResponseEntity<ErrorData> {
        val message = e.message ?: ""
        val exceptionType = e::class.simpleName
        log.error("$RUNTIME_ERROR $exceptionType 발생: $message", e)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR_CODE).body(ErrorData.of(e))
    }

    @ExceptionHandler(
        SQLException::class,
        IllegalStateException::class,
        BeansException::class,
        IOException::class
    )
    fun handleInternalException(e: Exception): ResponseEntity<ErrorData> {
        val prefix = when (e) {
            is SQLException -> "[DB 오류]"
            is IllegalStateException -> "[서버 상태 오류]"
            is BeansException -> "[Spring Bean 오류]"
            is IOException -> "[입출력 오류]"
            else -> INTERNAL_ERROR
        }
        log.error("$prefix ${e::class.simpleName} 발생: ${e.message}", e)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR_CODE).body(ErrorData.of(e))
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(e: Exception): ResponseEntity<ErrorData> =
        ResponseEntity.status(INTERNAL_SERVER_ERROR_CODE)
            .body(ErrorData.of(e))
            .also { log.error("$INTERNAL_ERROR 처리되지 않은 예외 발생: ${e.message}", e) }

    @ExceptionHandler(OutOfMemoryError::class, StackOverflowError::class)
    fun handleErrors(e: Error) {
        log.error("[심각] ${e::class.simpleName} 발생: ${e.message}", e)
    }
}