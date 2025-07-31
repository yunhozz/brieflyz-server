package io.brieflyz.api_gateway.filter

import io.brieflyz.api_gateway.exception.JwtTokenNotExistException
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.lang.Strings
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class AuthorizationHeaderFilter : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = logger()

    override fun apply(config: Config?): GatewayFilter =
        OrderedGatewayFilter({ exchange, chain ->
            val request = exchange.request
            val parsedToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

            log.debug("Parsed Token: $parsedToken")

            if (!Strings.hasText(parsedToken)) throw JwtTokenNotExistException()

            chain.filter(exchange)
        }, -1)

    class Config
}