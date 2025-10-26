package io.brieflyz.api_gateway.filter

import io.brieflyz.core.utils.logger
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CustomGlobalFilter : GlobalFilter {

    private val log = logger()

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val response = exchange.response

        log.info("[Global Filter Start] Request ID -> ${request.id}")
        log.info("Request URI : {}", request.uri)
        request.headers.forEach { (name, values) ->
            log.debug("Request Header '{}' = {}", name, values)
        }

        return chain.filter(exchange)
            .then(Mono.fromRunnable {
                log.info("[Global Filter End] Response Code -> ${response.statusCode}")
                response.headers.forEach { (name, values) ->
                    log.debug("Response Header '{}' = {}", name, values)
                }
            })
    }
}