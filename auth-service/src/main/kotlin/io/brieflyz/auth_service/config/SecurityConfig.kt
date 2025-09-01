package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.common.component.security.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.common.component.security.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.common.component.security.OAuthAuthorizationRequestCookieRepository
import io.brieflyz.auth_service.service.OAuthUserCustomService
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
    private val authServiceProperties: AuthServiceProperties,
    private val oAuthUserCustomService: OAuthUserCustomService,
    private val oAuthAuthorizationRequestCookieRepository: OAuthAuthorizationRequestCookieRepository,
    private val oAuthAuthenticationSuccessHandler: OAuthAuthenticationSuccessHandler,
    private val oAuthAuthenticationFailureHandler: OAuthAuthenticationFailureHandler
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
        .oauth2Login {
            it.userInfoEndpoint { cfg -> cfg.userService(oAuthUserCustomService) }
            it.authorizationEndpoint { cfg ->
                cfg.baseUri(authServiceProperties.oauth.authorizationUri)
                cfg.authorizationRequestRepository(oAuthAuthorizationRequestCookieRepository)
            }
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
        }
        .build()
}