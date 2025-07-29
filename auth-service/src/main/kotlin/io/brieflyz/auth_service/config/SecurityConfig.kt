package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.common.security.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.common.security.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.common.security.OAuthAuthorizationRequestCookieRepository
import io.brieflyz.auth_service.service.OAuthUserCustomService
import io.brieflyz.core.config.AuthServiceProperties
import io.brieflyz.core.config.JwtProperties
import io.jsonwebtoken.security.Keys
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import javax.crypto.SecretKey

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun secretKey(jwtProperties: JwtProperties): SecretKey {
        val secretKeyBytes = jwtProperties.secretKey.toByteArray()
        return Keys.hmacShaKeyFor(secretKeyBytes)
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        authServiceProperties: AuthServiceProperties,
        oAuthUserCustomService: OAuthUserCustomService,
        oAuthAuthorizationRequestCookieRepository: OAuthAuthorizationRequestCookieRepository,
        oAuthAuthenticationSuccessHandler: OAuthAuthenticationSuccessHandler,
        oAuthAuthenticationFailureHandler: OAuthAuthenticationFailureHandler
    ): SecurityFilterChain = http
        .cors { it.disable() }
        .csrf { it.disable() }
        .headers { it.frameOptions { cfg -> cfg.sameOrigin() } }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .oauth2Login {
            it.userInfoEndpoint { cfg -> cfg.userService(oAuthUserCustomService) }
            it.authorizationEndpoint { cfg ->
                cfg.baseUri(authServiceProperties.oauth?.authorizationUri)
                cfg.authorizationRequestRepository(oAuthAuthorizationRequestCookieRepository)
            }
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
        }
        .build()
}