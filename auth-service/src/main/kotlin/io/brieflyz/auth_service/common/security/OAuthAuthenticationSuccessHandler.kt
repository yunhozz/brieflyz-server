package io.brieflyz.auth_service.common.security

import io.brieflyz.auth_service.common.component.JwtProvider
import io.brieflyz.auth_service.common.component.RedisHandler
import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
import io.brieflyz.auth_service.config.AuthServiceProperties
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
    private val redisHandler: RedisHandler,
    private val oAuthAuthorizationRequestCookieRepository: OAuthAuthorizationRequestCookieRepository,
    private val authServiceProperties: AuthServiceProperties
) : SimpleUrlAuthenticationSuccessHandler() {

    private val log = logger()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val authorizedRedirectUris = authServiceProperties.oauth?.authorizedRedirectUris!!
        val requestedRedirectUri = CookieUtils.getCookie(request, CookieName.REDIRECT_URI_PARAM_COOKIE_NAME)?.value

        log.debug("Requested Redirect URI: $requestedRedirectUri")

        if (isNotAuthorizedRedirectUri(requestedRedirectUri, authorizedRedirectUris)) {
            handleUnauthorizedRedirect(request, response, requestedRedirectUri!!)
            return
        }

        val targetUrl = requestedRedirectUri ?: defaultTargetUrl
        val tokens = jwtProvider.generateToken(authentication)

        CookieUtils.addCookie(
            response,
            name = CookieName.ACCESS_TOKEN_COOKIE_NAME,
            value = SerializationUtils.serialize(tokens.accessToken),
            maxAge = tokens.accessTokenValidTime
        )
        redisHandler.save(authentication.name, tokens.refreshToken, tokens.refreshTokenValidTime)

        log.debug("Token Info: {}", tokens)
        log.info("OAuth2 Authentication Success, redirecting to: $targetUrl")

        cleanUpAuthenticationState(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun isNotAuthorizedRedirectUri(
        requestedRedirectUri: String?,
        authorizedRedirectUris: List<String>
    ): Boolean =
        if (
            requestedRedirectUri != null &&
            authorizedRedirectUris.none { requestedRedirectUri.startsWith(it) }
        ) {
            log.warn("Unauthorized redirect URI: $requestedRedirectUri")
            true
        } else false

    private fun handleUnauthorizedRedirect(
        request: HttpServletRequest,
        response: HttpServletResponse,
        requestedRedirectUri: String
    ) {
        val errorMsg = "승인되지 않은 리디렉션 URI가 있어 인증을 진행할 수 없습니다."
        val targetUrl = ServletUriComponentsBuilder.fromUriString(requestedRedirectUri)
            .queryParam("error", errorMsg)
            .toUriString()

        cleanUpAuthenticationState(request, response)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun cleanUpAuthenticationState(request: HttpServletRequest, response: HttpServletResponse) {
        clearAuthenticationAttributes(request)
        oAuthAuthorizationRequestCookieRepository.removeAuthorizationRequestCookies(request, response)
    }
}