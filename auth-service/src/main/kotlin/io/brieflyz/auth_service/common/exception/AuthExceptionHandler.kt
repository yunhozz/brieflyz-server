package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorCode
import io.brieflyz.core.utils.logger
import org.springframework.beans.BeansException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.sql.SQLException

@RestControllerAdvice
class AuthExceptionHandler {

    private val log = logger()

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse.ErrorData> {
        val message = e.localizedMessage
        log.warn("[인증 서비스 예외] $message")
        return ResponseEntity.status(e.errorCode.status).body(ApiResponse.ErrorData.of(message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse.ErrorData> {
        val message = e.localizedMessage
        val errorData = ApiResponse.ErrorData.of(message, ApiResponse.ErrorData.FieldError.of(e.bindingResult))
        log.warn("[Validation 오류] $message")
        return ResponseEntity.status(ErrorCode.BAD_REQUEST.status).body(errorData)
    }

    @ExceptionHandler(NullPointerException::class, IndexOutOfBoundsException::class)
    fun handleRuntimeException(e: RuntimeException) {
        when (e) {
            is NullPointerException ->
                log.error("[런타임 오류] NullPointerException 발생: ${e.message}", e)

            is IndexOutOfBoundsException ->
                log.error("[런타임 오류] IndexOutOfBoundsException 발생: ${e.message}", e)

            else ->
                log.error("[런타임 오류] 처리되지 않은 RuntimeException 발생: ${e.message}", e)
        }
        throw e
    }

    @ExceptionHandler(SQLException::class)
    fun handleDatabaseException(e: SQLException) {
        log.error("[DB 오류] SQL 예외 발생: ${e.message}", e)
        throw e
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException) {
        log.error("[서버 상태 오류] IllegalStateException 발생: ${e.message}", e)
        throw e
    }

    @ExceptionHandler(OutOfMemoryError::class)
    fun handleOutOfMemory(e: OutOfMemoryError) {
        log.error("[메모리 오류] OutOfMemoryError 발생: ${e.message}", e)
        throw e
    }

    @ExceptionHandler(BeansException::class)
    fun handleSpringBeansException(e: BeansException) {
        log.error("[Spring Bean 오류] BeansException 발생: ${e.message}", e)
        throw e
    }

    @ExceptionHandler(IOException::class)
    fun handleIOException(e: IOException) {
        log.error("[입출력 오류] IOException 발생: ${e.message}", e)
        throw e
    }

    @ExceptionHandler(Exception::class)
    fun handleOtherExceptions(e: Exception) {
        log.error("[서버 내부 오류] 처리되지 않은 예외 발생: ${e.message}", e)
        throw e
    }
}