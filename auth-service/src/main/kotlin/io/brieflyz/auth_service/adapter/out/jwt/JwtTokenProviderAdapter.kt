package io.brieflyz.auth_service.adapter.out.jwt

import io.brieflyz.auth_service.application.dto.result.PrincipalResult
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.core.beans.jwt.JwtManager
import io.brieflyz.core.constants.Role
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProviderAdapter(
    private val jwtManager: JwtManager
) : TokenProviderPort {

    private val log = logger()

    override fun generateToken(username: String, roles: String): TokenResult {
        val now = Date()
        val roles = roles.split("|")
            .filter { it.isNotBlank() }
            .map { Role.of(it) }

        val secretKey = jwtManager.getEncryptedSecretKey()
        val (_, tokenType, accessTokenValidTime, refreshTokenValidTime) = jwtManager.getProperties()

        val accessToken = createToken(secretKey, username, roles, now, accessTokenValidTime)
        val refreshToken = createToken(secretKey, username, roles, now, refreshTokenValidTime)

        log.debug("secret key: ${Encoders.BASE64URL.encode(secretKey.encoded)}")
        log.debug("access token: $accessToken")
        log.debug("refresh token: $refreshToken")

        return TokenResult(tokenType, accessToken, refreshToken, accessTokenValidTime, refreshTokenValidTime)
    }

    override fun getPrincipal(token: String): PrincipalResult {
        val claims = jwtManager.createClaimsJws(token)?.body
            ?: throw IllegalArgumentException("Invalid token")
        val roles = (claims["roles"] as? List<String>) ?: emptyList()
        return PrincipalResult(claims.subject, roles)
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