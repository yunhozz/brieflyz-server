package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.adapter.`in`.security.OAuth2UserCustomService
import io.brieflyz.auth_service.adapter.`in`.security.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.adapter.`in`.security.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.adapter.`in`.security.OAuthAuthorizationRequestCookieRepository
import io.brieflyz.auth_service.common.props.AuthServiceProperties
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
    private val oAuth2UserCustomService: OAuth2UserCustomService,
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
            it.userInfoEndpoint { cfg -> cfg.userService(oAuth2UserCustomService) }
            it.authorizationEndpoint { cfg ->
                cfg.baseUri(authServiceProperties.oauth.authorizationUri)
                cfg.authorizationRequestRepository(oAuthAuthorizationRequestCookieRepository)
            }
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
        }
        .build()
}