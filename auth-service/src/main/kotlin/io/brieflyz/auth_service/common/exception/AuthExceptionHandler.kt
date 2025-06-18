package io.brieflyz.auth_service.common.exception

import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.utils.logger
import org.springframework.beans.BeansException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.io.IOException
import java.sql.SQLException

@RestControllerAdvice
class AuthExceptionHandler {

    private val log = logger()

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ApiResponse.ErrorData> {
        log.warn(e.message)
        return ResponseEntity.status(e.errorCode.status)
            .body(ApiResponse.ErrorData.of(e.localizedMessage))
    }

    @ExceptionHandler(NullPointerException::class, IndexOutOfBoundsException::class)
    fun handleRuntimeException(e: RuntimeException) {
        log.error(e.message)
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
        log.error("[심각] OutOfMemoryError 발생: ${e.message}", e)
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