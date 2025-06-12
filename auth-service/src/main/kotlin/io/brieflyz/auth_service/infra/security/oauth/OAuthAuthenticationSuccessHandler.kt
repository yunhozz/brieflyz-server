package io.brieflyz.auth_service.infra.security.oauth

import io.brieflyz.auth_service.config.AppConfig
import io.brieflyz.auth_service.infra.security.jwt.JwtProvider
import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@Component
class OAuthAuthenticationSuccessHandler(
    private val jwtProvider: JwtProvider,
    private val appConfig: AppConfig
) : SimpleUrlAuthenticationSuccessHandler() {

    private val log = logger()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val authorizedRedirectUris = appConfig.oauth.authorizedRedirectUris
        val requestedRedirectUri = request.getParameter("redirect_uri")
        log.debug("Redirect URI: $requestedRedirectUri")

        if (isNotRedirectUri(requestedRedirectUri, authorizedRedirectUris)) return

        val tokens = jwtProvider.generateToken(authentication)
        val targetUrl = ServletUriComponentsBuilder.fromUriString(requestedRedirectUri)
            .queryParam("token_type", tokens.tokenType)
            .queryParam("access_token", tokens.accessToken)
            .queryParam("refresh_token", tokens.refreshToken)
            .toUriString()

        log.debug("Token Info: {}", tokens)
        log.info("OAuth2 Authentication Success, redirecting to: $targetUrl")

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun isNotRedirectUri(
        requestedRedirectUri: String?,
        authorizedRedirectUris: List<String>
    ): Boolean =
        if (requestedRedirectUri == null
            || authorizedRedirectUris.none { requestedRedirectUri.startsWith(it) }
        ) {
            log.warn("Unauthorized redirect URI: $requestedRedirectUri")
            true
        } else false
}