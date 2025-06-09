package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.auth_service.config.AppProperties
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val appProperties: AppProperties
) {
    private val log = logger()

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initSecretKey() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    fun generateToken(username: String): JwtTokens {
        val now = Date()
        val tokenType = appProperties.jwt.tokenType
        val accessTokenValidTime: Long = appProperties.jwt.accessTokenValidTime
        val refreshTokenValidTime: Long = appProperties.jwt.refreshTokenValidTime

        log.debug("secret key: ${Encoders.BASE64URL.encode(secretKey.encoded)}")

        val accessToken = createToken(now, username, accessTokenValidTime)
        val refreshToken = createToken(now, username, refreshTokenValidTime)

        log.debug("access token: $accessToken")
        log.debug("refresh token: $refreshToken")

        return JwtTokens(tokenType, accessToken, refreshToken, accessTokenValidTime, refreshTokenValidTime)
    }

    fun isTokenValid(token: String): Boolean =
        try {
            createClaimsJws(token)
            true
        } catch (e: Exception) {
            log.warn("Invalid JWT token: ${e.message}")
            false
        }

    fun getAuthentication(token: String): Authentication {
        val claims = createClaimsJws(token).body
        val username = claims.subject
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER")) // TODO: 권한 설정

        return UsernamePasswordAuthenticationToken(username, token, authorities)
    }

    private fun createToken(now: Date, username: String, tokenValidTime: Long): String =
        Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()

    private fun createClaimsJws(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
}