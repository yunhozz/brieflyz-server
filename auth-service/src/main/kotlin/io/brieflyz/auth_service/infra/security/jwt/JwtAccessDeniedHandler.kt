package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class JwtAccessDeniedHandler : AccessDeniedHandler {

    private val log = logger()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        log.warn(
            "Access denied: method={}, uri={}, remoteAddr={}, message={}",
            request.method,
            request.requestURI,
            request.remoteAddr,
            accessDeniedException.message
        )
        log.debug("Exception: ", accessDeniedException)
        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.message)
    }
}