package io.brieflyz.core.beans.jwt

import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.lang.Strings
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtManager(
    private val jwtProperties: JwtProperties
) {
    private val log = logger()

    private val secretKey: SecretKey by lazy {
        val secretKeyBytes = jwtProperties.secretKey.toByteArray()
        Keys.hmacShaKeyFor(secretKeyBytes)
    }

    fun getEncryptedSecretKey() = secretKey

    fun getProperties() = jwtProperties

    fun resolveToken(token: String?): String? =
        token.takeIf { Strings.hasText(it) }?.let {
            val parts = it.split(" ")
            val tokenType = jwtProperties.tokenType
            return if (parts.size == 2 && parts[0] == tokenType.trim()) parts[1] else null
        }

    fun createClaimsJws(token: String): Jws<Claims>? =
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)

        } catch (e: JwtException) {
            log.warn(
                """
                [Invalid JWT Token]
                Exception Class: ${e.javaClass.simpleName}
                Message: ${e.message}
            """.trimIndent()
            )
            null
        }
}