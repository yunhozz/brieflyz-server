package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {

    private val log = logger()

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.warn(
            "Access denied: method={}, uri={}, remoteAddr={}, message={}",
            request.method,
            request.requestURI,
            request.remoteAddr,
            authException.message
        )
        log.debug("Exception: ", authException)
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
    }
}