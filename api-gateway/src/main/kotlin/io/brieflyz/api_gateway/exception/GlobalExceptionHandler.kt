package io.brieflyz.api_gateway.exception

import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.ErrorCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorData
import io.brieflyz.core.utils.logger
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.ResolvableType
import org.springframework.core.annotation.Order
import org.springframework.core.codec.Hints
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
@Order(-1)
class GlobalExceptionHandler(
    private val objectMapper: ObjectMapper
) : ErrorWebExceptionHandler {

    private val log = logger()

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void?> {
        val response = exchange.response

        val apiResponse = when (ex) {
            is ApiGatewayException -> {
                log.warn("[API Gateway 예외] ${ex.localizedMessage}")
                val errorCode = ex.errorCode
                response.statusCode = HttpStatusCode.valueOf(errorCode.status)
                ApiResponse.fail(errorCode, ErrorData.of(ex))
            }

            else -> {
                log.error(ex.message, ex)
                response.statusCode = HttpStatus.SERVICE_UNAVAILABLE
                ApiResponse.fail(ErrorCode.SERVICE_UNAVAILABLE, ErrorData.of(Exception(ex)))
            }
        }

        response.headers.contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)

        return response.writeWith(
            Jackson2JsonEncoder(objectMapper).encode(
                Mono.just(apiResponse),
                response.bufferFactory(),
                ResolvableType.forInstance(apiResponse),
                MediaType.APPLICATION_JSON,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix)
            )
        )
    }
}