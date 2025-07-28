package io.brieflyz.subscription_service.config.web

import io.brieflyz.core.config.JwtProperties
import io.brieflyz.subscription_service.common.annotation.JwtHeader
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import javax.crypto.SecretKey

@Component
class JwtHeaderResolver(
    private val jwtProperties: JwtProperties
) : HandlerMethodArgumentResolver {

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initSecretKey() {
        secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey.toByteArray())
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(JwtHeader::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val token = webRequest.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return parseToken(token)?.subject
    }

    private fun parseToken(token: String): Claims? = try {
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    } catch (e: JwtException) {
        throw JwtException("JWT 토큰 파싱 오류: ${e.localizedMessage}", e)
    }
}