package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.auth_service.config.AppConfig
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.lang.Strings
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtProvider: JwtProvider,
    private val appProperties: AppConfig
) : OncePerRequestFilter() {

    private val tokenType = appProperties.jwt.tokenType
    private val log = logger()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.debug(
            request.headerNames.toList()
                .associateWith { request.getHeader(it) }
                .toString()
        )
        log.info("Request URI: [${request.method}] ${request.requestURI}")

        val headerToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        log.debug("Header Token: $headerToken")

        resolveToken(headerToken)?.let { token ->
            log.debug("Parsed Token: $token")
            if (jwtProvider.isTokenValid(token)) {
                SecurityContextHolder.getContext().authentication = jwtProvider.getAuthentication(token)
            }
        } ?: SecurityContextHolder.clearContext()

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(token: String?): String? =
        token.takeIf { Strings.hasText(it) }?.let { resolveTokenParts(it) }

    private fun resolveTokenParts(token: String): String? {
        val parts = token.split(" ")
        return if (parts.size == 2 && parts[0] == tokenType) parts[1] else null
    }
}