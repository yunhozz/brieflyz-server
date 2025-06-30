package io.brieflyz.auth_service.infra.security.oauth

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class OAuth2AuthorizationRequestCookieRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private val log = logger()

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        CookieUtils.getCookie(request, CookieName.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)?.let { cookie ->
            CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest::class.java)
        }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        authorizationRequest?.let { authRequest ->
            log.debug("Authorization Request URI: ${authRequest.authorizationRequestUri}")
            log.debug("Authorization URI: ${authRequest.authorizationUri}")
            log.debug("redirect URI: ${authRequest.redirectUri}")

            CookieUtils.addCookie(
                response,
                name = CookieName.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                value = CookieUtils.serialize(authRequest),
                maxAge = CookieName.COOKIE_EXPIRE_MILLIS
            )

            request.getParameter(CookieName.REDIRECT_URI_PARAM_COOKIE_NAME)?.let { redirectUriAfterLogin ->
                log.debug("Redirect URI after Login: $redirectUriAfterLogin")
                CookieUtils.addCookie(
                    response,
                    name = CookieName.REDIRECT_URI_PARAM_COOKIE_NAME,
                    value = redirectUriAfterLogin,
                    maxAge = CookieName.COOKIE_EXPIRE_MILLIS
                )
            }

        } ?: run {
            removeAuthorizationRequest(request, response)
        }
    }

    override fun removeAuthorizationRequest(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): OAuth2AuthorizationRequest? = this.loadAuthorizationRequest(request)

    fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        CookieUtils.deleteCookie(request, response, CookieName.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
        CookieUtils.deleteCookie(request, response, CookieName.REDIRECT_URI_PARAM_COOKIE_NAME)
    }
}