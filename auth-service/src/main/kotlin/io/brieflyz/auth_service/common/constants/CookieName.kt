package io.brieflyz.auth_service.common.constants

import kotlin.reflect.full.declaredMemberProperties

object CookieName {
    const val ACCESS_TOKEN_COOKIE_NAME = "atk"
    const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
    const val REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri"
    const val COOKIE_EXPIRE_MILLIS = 180 * 1000L // 3 min

    val members: List<String> = CookieName::class.declaredMemberProperties
        .filter { it.returnType.classifier == String::class }
        .mapNotNull { it.getter.call() as? String }
}