package io.brieflyz.auth_service.infra.security.oauth

import com.nimbusds.jose.util.StandardCharset
import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URLEncoder

@Component
class OAuthAuthenticationFailureHandler(
    private val oAuthAuthorizationRequestCookieRepository: OAuthAuthorizationRequestCookieRepository
) : SimpleUrlAuthenticationFailureHandler() {

    private val log = logger()

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        if (response == null) return

        val redirectUri = request.getParameter("redirect_uri")
        val errorMsg = exception?.localizedMessage ?: "Authentication failed"

        log.debug("Redirect URI: $redirectUri")
        log.debug("Error Message: $errorMsg")

        if (redirectUri != null) {
            val targetUrl = ServletUriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("error", URLEncoder.encode(errorMsg, StandardCharset.UTF_8))
                .toUriString()

            log.error("OAuth2 Authentication Fail: $errorMsg, Redirect URL: $targetUrl")

            oAuthAuthorizationRequestCookieRepository.removeAuthorizationRequestCookies(request, response)
            redirectStrategy.sendRedirect(request, response, targetUrl)

        } else {
            log.error("OAuth2 Authentication Fail: $errorMsg")
        }
    }
}