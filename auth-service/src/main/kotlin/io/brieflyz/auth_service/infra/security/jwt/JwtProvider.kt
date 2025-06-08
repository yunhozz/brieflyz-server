package io.brieflyz.auth_service.infra.security.jwt

import io.brieflyz.auth_service.config.AppProperties
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import jakarta.annotation.PostConstruct
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
    fun test() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256)
    }

    fun generateToken(username: String): JwtTokens {
        val now = Date()
        val tokenType = appProperties.jwt.tokenType ?: ""
        val accessTokenValidTime: Long = appProperties.jwt.accessTokenValidTime ?: 600000L
        val refreshTokenValidTime: Long = appProperties.jwt.refreshTokenValidTime ?: 3600000L

        log.debug("secret key: ${Encoders.BASE64URL.encode(secretKey.encoded)}")

        val accessToken = createToken(username, accessTokenValidTime)
        val refreshToken = createToken(username, refreshTokenValidTime)

        log.debug("\naccess token: $accessToken\nrefresh token: $refreshToken")

        return JwtTokens(tokenType, accessToken, refreshToken, accessTokenValidTime, refreshTokenValidTime)
    }

    private fun createToken(username: String, tokenValidTime: Long): String {
        val now = Date()
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(Date(now.time + tokenValidTime))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact()
    }
}