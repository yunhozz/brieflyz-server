package io.brieflyz.auth_service.common.component.security

import io.brieflyz.auth_service.common.constants.CookieName
import io.brieflyz.auth_service.common.utils.CookieUtils
import io.brieflyz.auth_service.common.utils.SerializationUtils
import io.brieflyz.core.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class OAuthAuthorizationRequestCookieRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private val log = logger()

    companion object {
        private const val COOKIE_EXPIRE_MILLIS = 180 * 1000L // 3 min
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? =
        CookieUtils.getCookie(request, CookieName.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)?.let { cookie ->
            SerializationUtils.deserialize(cookie.value, OAuth2AuthorizationRequest::class.java)
        }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        authorizationRequest?.let { authRequest ->
            log.debug("OAuth2.0 Authorization Request URI: ${authRequest.authorizationRequestUri}")
            log.debug("OAuth2.0 Authorization URI: ${authRequest.authorizationUri}")
            log.debug("OAuth2.0 Redirect URI: ${authRequest.redirectUri}")

            CookieUtils.addCookie(
                response,
                name = CookieName.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                value = SerializationUtils.serialize(authRequest),
                maxAge = COOKIE_EXPIRE_MILLIS
            )

            request.getParameter(CookieName.REDIRECT_URI_PARAM_COOKIE_NAME)?.let { redirectUri ->
                log.debug("Requested Redirect URI: $redirectUri")
                CookieUtils.addCookie(
                    response,
                    name = CookieName.REDIRECT_URI_PARAM_COOKIE_NAME,
                    value = redirectUri,
                    maxAge = COOKIE_EXPIRE_MILLIS
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