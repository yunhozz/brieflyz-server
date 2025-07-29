package io.brieflyz.auth_service.config

import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import javax.crypto.SecretKey

@Configuration
class WebConfig(
    private val jwtHeaderResolver: JwtHeaderResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(jwtHeaderResolver)
    }
}

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
        parameter.hasParameterAnnotation(JwtSubject::class.java)

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