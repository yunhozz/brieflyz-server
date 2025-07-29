package io.brieflyz.api_gateway.filter

import io.brieflyz.api_gateway.exception.JwtTokenNotExistException
import io.brieflyz.api_gateway.exception.JwtTokenNotValidException
import io.brieflyz.core.config.JwtProperties
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class AuthorizationHeaderFilter(
    private val jwtProperties: JwtProperties
) : AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config>(Config::class.java) {

    private val log = logger()

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initSecretKey() {
        secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    }

    override fun apply(config: Config?): GatewayFilter = OrderedGatewayFilter({ exchange, chain ->
        val request = exchange.request
        val parsedToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        log.debug("Parsed Token: $parsedToken")

        parsedToken?.let { token ->
            log.debug("Parsed Token: $token")
            if (!isTokenValid(token)) throw JwtTokenNotValidException()

            chain.filter(exchange)

        } ?: throw JwtTokenNotExistException()
    }, -1)

    private fun isTokenValid(token: String): Boolean =
        try {
            createClaimsJws(token)
            true

        } catch (e: Exception) {
            log.warn(
                """
                [Invalid JWT Token]
                Exception Class: ${e.javaClass.simpleName}
                Message: ${e.message}
            """.trimIndent()
            )
            false
        }

    private fun createClaimsJws(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)

    class Config
}