package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthorizationRequestCookieRepository
import io.brieflyz.auth_service.infra.security.oauth.OAuthUserCustomService
import io.brieflyz.auth_service.infra.security.user.CustomUserDetailsService
import io.brieflyz.core.config.AuthServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: CustomUserDetailsService,
    private val oAuthAuthenticationSuccessHandler: OAuthAuthenticationSuccessHandler,
    private val oAuthAuthenticationFailureHandler: OAuthAuthenticationFailureHandler,
    private val oAuthUserCustomService: OAuthUserCustomService,
    private val oAuthAuthorizationRequestCookieRepository: OAuthAuthorizationRequestCookieRepository,
    private val authServiceProperties: AuthServiceProperties
) {
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.disable() }
        .csrf { it.disable() }
        .headers { it.frameOptions { cfg -> cfg.sameOrigin() } }
        .httpBasic { it.disable() }
        .formLogin { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .userDetailsService(userDetailsService)
        .oauth2Login {
            it.authorizationEndpoint { cfg ->
                cfg.baseUri(authServiceProperties.oauth?.authorizationUri)
                cfg.authorizationRequestRepository(oAuthAuthorizationRequestCookieRepository)
            }
            it.userInfoEndpoint { cfg -> cfg.userService(oAuthUserCustomService) }
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
        }
        .build()
}