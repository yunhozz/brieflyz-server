package io.brieflyz.auth_service.common.utils

import io.brieflyz.auth_service.common.constants.CookieName
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.server.Cookie.SameSite
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.util.SerializationUtils
import java.io.ByteArrayInputStream
import java.io.ObjectInputStream
import java.time.Duration
import java.util.Base64

object CookieUtils {
    fun getCookie(request: HttpServletRequest, name: String): Cookie? =
        request.cookies?.find { it.name == name }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Long?) {
        val cookieBuilder = ResponseCookie.from(name, value)
            .path("/")
            .httpOnly(true)
            .secure(false)
            .sameSite(SameSite.LAX.attributeValue())
            .maxAge(maxAge?.let { Duration.ofMillis(it) } ?: Duration.ofDays(1))

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString())
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) =
        request.cookies?.find { it.name == name }?.apply {
            value = null
            path = "/"
            maxAge = 0
            response.addCookie(this)
        }

    fun deleteAllCookies(request: HttpServletRequest, response: HttpServletResponse) {
        request.cookies?.forEach { cookie ->
            val cookieName = cookie.name
            if (cookieName in CookieName.members) {
                Cookie(cookieName, null).apply {
                    path = "/"
                    maxAge = 0
                    response.addCookie(this)
                }
            }
        }
    }

    fun serialize(obj: Any): String {
        val bytes = SerializationUtils.serialize(obj)
        return Base64.getUrlEncoder().encodeToString(bytes)
    }

    fun <T> deserialize(cookie: Cookie, clazz: Class<T>): T {
        val bytes = Base64.getUrlDecoder().decode(cookie.value)
        ByteArrayInputStream(bytes).use { bais ->
            ObjectInputStream(bais).use { ois ->
                return clazz.cast(ois.readObject())
            }
        }
    }
}