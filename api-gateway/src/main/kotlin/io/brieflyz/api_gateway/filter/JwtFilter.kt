package io.brieflyz.api_gateway.filter

import io.brieflyz.core.config.JwtProperties
import io.brieflyz.core.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.lang.Strings
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import javax.crypto.SecretKey

@Component
class JwtFilter(
    private val jwtProperties: JwtProperties,
    private val secretKey: SecretKey
) : WebFilter {

    private val log = logger()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void?> {
        val request = exchange.request
        log.info("Request URI: [${request.method}] ${request.uri}")

        val headerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        log.debug("Header Token: $headerToken")

        return resolveToken(headerToken)?.let { token ->
            val claims = createClaimsJws(token).body
            val authorities = claims["roles"] as List<String>

            log.debug("claims = {}", claims)

            val userDetails = CustomUserDetails(claims.subject, authorities)
            val authentication = UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)

            val mutatedExchange = exchange.mutate()
                .request(
                    request.mutate()
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .build()
                )
                .build()

            val securityContext = ReactiveSecurityContextHolder.withAuthentication(authentication)

            chain.filter(mutatedExchange).contextWrite(securityContext)

        } ?: return chain.filter(exchange)
    }

    private fun resolveToken(token: String?): String? =
        token.takeIf { Strings.hasText(it) }?.let {
            val parts = it.split(" ")
            val tokenType = jwtProperties.tokenType
            return if (parts.size == 2 && parts[0] == tokenType.trim()) parts[1] else null
        }

    private fun createClaimsJws(token: String): Jws<Claims> =
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)

    data class CustomUserDetails(
        private val username: String,
        private val authorities: List<String>
    ) : UserDetails {
        override fun getUsername() = username
        override fun getPassword() = null
        override fun getAuthorities() = authorities
            .map { auth -> SimpleGrantedAuthority(auth) }.toMutableSet()

        override fun isAccountNonExpired() = true
        override fun isAccountNonLocked() = true
        override fun isCredentialsNonExpired() = true
        override fun isEnabled() = true
    }
}