package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.core.utils.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtFilter(
    private val jwtProvider: JwtProvider
) : OncePerRequestFilter() {

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

        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (token != null) {
            val authentication = jwtProvider.getAuthentication(token)
            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }
}