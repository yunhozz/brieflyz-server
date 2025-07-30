package io.brieflyz.core.component

import io.brieflyz.core.config.JwtProperties
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.lang.Strings
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtManager(
    val jwtProperties: JwtProperties
) : InitializingBean {

    private val log = logger()

    private lateinit var secretKey: SecretKey

    override fun afterPropertiesSet() {
        val secretKeyBytes = jwtProperties.secretKey.toByteArray()
        secretKey = Keys.hmacShaKeyFor(secretKeyBytes)
    }

    fun getEncryptedSecretKey() = secretKey

    fun resolveToken(token: String?): String? =
        token.takeIf { Strings.hasText(it) }?.let {
            val parts = it.split(" ")
            val tokenType = jwtProperties.tokenType
            return if (parts.size == 2 && parts[0] == tokenType.trim()) parts[1] else null
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

    fun createClaimsJws(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
}