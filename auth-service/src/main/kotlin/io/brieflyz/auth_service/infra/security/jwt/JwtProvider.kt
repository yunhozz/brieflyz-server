package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.auth_service.config.AppConfig
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
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val appConfig: AppConfig
) {
    private val log = logger()

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initSecretKey() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    fun generateToken(authentication: Authentication): JwtTokens {
        val now = Date()
        val tokenType = appConfig.jwt.tokenType
        val accessTokenValidTime: Long = appConfig.jwt.accessTokenValidTime
        val refreshTokenValidTime: Long = appConfig.jwt.refreshTokenValidTime

        val accessToken = createToken(now, authentication, accessTokenValidTime)
        val refreshToken = createToken(now, authentication, refreshTokenValidTime)

        log.debug("secret key: ${Encoders.BASE64URL.encode(secretKey.encoded)}")
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
        val roles = claims["roles"] as? List<String>

        log.debug("username: $username")
        log.debug("roles: {}", roles)
        log.debug("authorities: {}", AuthorityUtils.createAuthorityList(roles))

        return UsernamePasswordAuthenticationToken(username, "", AuthorityUtils.createAuthorityList(roles))
    }

    private fun createToken(iat: Date, authentication: Authentication, tokenValidTime: Long): String =
        Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setSubject(authentication.name)
            .claim("roles", authentication.authorities.map { it.authority })
            .setIssuedAt(iat)
            .setExpiration(Date(iat.time + tokenValidTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()

    private fun createClaimsJws(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
}