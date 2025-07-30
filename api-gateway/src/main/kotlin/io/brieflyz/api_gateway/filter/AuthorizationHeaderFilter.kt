package io.brieflyz.api_gateway.filter

import io.brieflyz.api_gateway.exception.JwtTokenNotExistException
import io.brieflyz.api_gateway.exception.JwtTokenNotValidException
import io.brieflyz.core.component.JwtManager
import io.brieflyz.core.utils.logger
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class AuthorizationHeaderFilter(
    private val jwtManager: JwtManager
) : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = logger()

    override fun apply(config: Config?): GatewayFilter = OrderedGatewayFilter({ exchange, chain ->
        val request = exchange.request
        val parsedToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        log.debug("Parsed Token: $parsedToken")

        parsedToken?.let { token ->
            log.debug("Parsed Token: $token")
            if (!jwtManager.isTokenValid(token)) throw JwtTokenNotValidException()
            chain.filter(exchange)

        } ?: throw JwtTokenNotExistException()
    }, -1)

    class Config
}