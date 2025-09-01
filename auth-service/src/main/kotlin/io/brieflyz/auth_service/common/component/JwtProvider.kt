package io.brieflyz.auth_service.common.component

import io.brieflyz.auth_service.common.exception.JwtTokenNotValidException
import io.brieflyz.auth_service.model.security.CustomUserDetails
import io.brieflyz.core.beans.jwt.JwtManager
import io.brieflyz.core.constants.Role
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtProvider(
    private val jwtManager: JwtManager
) {
    private val log = logger()

    data class JwtTokens(
        val tokenType: String,
        val accessToken: String,
        val refreshToken: String,
        val accessTokenValidTime: Long,
        val refreshTokenValidTime: Long
    )

    fun generateToken(username: String, rolesStr: String): JwtTokens {
        val now = Date()
        val roles = rolesStr.split("|")
            .filter { it.isNotBlank() }
            .map { Role.of(it) }

        val secretKey = jwtManager.getEncryptedSecretKey()
        val (_, tokenType, accessTokenValidTime, refreshTokenValidTime) = jwtManager.getProperties()

        val accessToken = createToken(secretKey, username, roles, now, accessTokenValidTime)
        val refreshToken = createToken(secretKey, username, roles, now, refreshTokenValidTime)

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

    fun getAuthentication(token: String): Authentication {
        val claims = jwtManager.createClaimsJws(token)?.body
            ?: throw JwtTokenNotValidException()
        val roles = claims["roles"] as List<String>
        val userDetails = CustomUserDetails(claims.subject, roles)

        log.debug(
            "[User Details] username={}, authorities={}",
            userDetails.username,
            userDetails.authorities
        )

        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    private fun createToken(
        secretKey: SecretKey,
        username: String,
        roles: List<Role>,
        iat: Date,
        tokenValidTime: Long
    ): String = Jwts.builder()
        .setHeaderParam("typ", "JWT")
        .setSubject(username)
        .claim("roles", roles)
        .setIssuedAt(iat)
        .setExpiration(Date(iat.time + tokenValidTime))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact()
}