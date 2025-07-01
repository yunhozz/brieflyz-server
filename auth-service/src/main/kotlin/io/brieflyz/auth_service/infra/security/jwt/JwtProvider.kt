package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.auth_service.infra.security.user.CustomUserDetailsService
import io.brieflyz.core.config.AuthServiceProperties
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
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val authServiceProperties: AuthServiceProperties,
    private val userDetailsService: CustomUserDetailsService
) {
    private val log = logger()

    private lateinit var secretKey: SecretKey

    @PostConstruct
    fun initSecretKey() {
        secretKey = Keys.hmacShaKeyFor(authServiceProperties.jwt?.secretKey?.toByteArray())
    }

    fun generateToken(username: String, rolesStr: String): JwtTokens {
        val now = Date()
        val jwtProperties = authServiceProperties.jwt!!

        val roles = rolesStr.split("|")
        val tokenType = jwtProperties.tokenType
        val accessTokenValidTime = jwtProperties.accessTokenValidTime
        val refreshTokenValidTime = jwtProperties.refreshTokenValidTime

        val accessToken = createToken(username, roles, now, accessTokenValidTime)
        val refreshToken = createToken(username, roles, now, refreshTokenValidTime)

        log.debug("secret key: ${Encoders.BASE64URL.encode(secretKey.encoded)}")
        log.debug("access token: $accessToken")
        log.debug("refresh token: $refreshToken")

        return JwtTokens(tokenType, accessToken, refreshToken, accessTokenValidTime, refreshTokenValidTime)
    }

    fun generateToken(authentication: Authentication): JwtTokens {
        val username = authentication.name
        val roles = authentication.authorities.joinToString("|") { it.authority }
        return generateToken(username, roles)
    }

    fun isTokenValid(token: String): Boolean =
        try {
            createClaimsJws(token)
            true

        } catch (e: Exception) {
            log.warn(
                """
                [Invalid JWT Token]
                Exception Class: ${e.javaClass.simpleName}
                Message: ${e.message}
            """.trimIndent()
            )
            false
        }

    fun getAuthentication(token: String): Authentication {
        val claims = createClaimsJws(token).body
        val userDetails = userDetailsService.loadUserByUsername(claims.subject)

        log.debug(
            """
            [User Details]
            username: ${userDetails.username}
            authorities: ${userDetails.authorities}
        """.trimIndent()
        )

        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    private fun createToken(username: String, roles: List<String>, iat: Date, tokenValidTime: Long): String =
        Jwts.builder()
            .setHeaderParam("typ", "JWT")
            .setSubject(username)
            .claim("roles", roles)
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