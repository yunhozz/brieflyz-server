package io.brieflyz.api_gateway.exception

import com.fasterxml.jackson.databind.ObjectMapper
import io.brieflyz.core.constants.ErrorCode
import io.brieflyz.core.dto.api.ApiResponse
import io.brieflyz.core.dto.api.ErrorData
import io.brieflyz.core.utils.logger
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Hints
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

@Component
class JwtAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : ServerAccessDeniedHandler {

    private val log = logger()

    override fun handle(
        exchange: ServerWebExchange,
        ex: AccessDeniedException
    ): Mono<Void?>? {
        val request = exchange.request
        val response = exchange.response

        log.warn(
            "Access denied: method={}, uri={}, remoteAddr={}, message={}",
            request.method,
            request.uri,
            request.remoteAddress,
            ex.message
        )

        val errorCode = ErrorCode.FORBIDDEN
        val apiResponse = ApiResponse.fail(errorCode, ErrorData.of(ex))

        response.headers.contentType = MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8)
        response.statusCode = HttpStatusCode.valueOf(errorCode.status)

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