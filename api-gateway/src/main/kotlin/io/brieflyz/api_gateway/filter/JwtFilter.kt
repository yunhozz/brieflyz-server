package io.brieflyz.api_gateway.filter

import io.brieflyz.api_gateway.exception.JwtTokenNotValidException
import io.brieflyz.core.beans.jwt.JwtManager
import io.brieflyz.core.utils.logger
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

@Component
class JwtFilter(
    private val jwtManager: JwtManager
) : WebFilter {

    private val log = logger()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void?> {
        val request = exchange.request
        log.info("Request URI: [${request.method}] ${request.uri}")

        val headerToken = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        log.debug("Header Token: $headerToken")

        return jwtManager.resolveToken(headerToken)?.let { token ->
            log.debug("Resolved token : $token")
            val claims = jwtManager.createClaimsJws(token)?.body
                ?: throw JwtTokenNotValidException()
            val authorities = claims["roles"] as List<String>

            log.debug("claims : {}", claims)

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

    data class CustomUserDetails(
        private val username: String,
        private val authorities: List<String>
    ) : UserDetails {
        override fun getUsername() = username
        override fun getPassword() = null
        override fun getAuthorities() = authorities
            .map { auth -> SimpleGrantedAuthority(auth) }
            .toMutableSet()

        override fun isAccountNonExpired() = true
        override fun isAccountNonLocked() = true
        override fun isCredentialsNonExpired() = true
        override fun isEnabled() = true
    }
}