package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.common.constants.Role
import io.brieflyz.auth_service.infra.security.jwt.JwtAccessDeniedHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtAuthenticationEntryPoint
import io.brieflyz.auth_service.infra.security.jwt.JwtFilter
import io.brieflyz.auth_service.infra.security.oauth.OAuth2AuthorizationRequestCookieRepository
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthUserCustomService
import io.brieflyz.core.config.AuthServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtFilter: JwtFilter,
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
    private val oAuthAuthenticationSuccessHandler: OAuthAuthenticationSuccessHandler,
    private val oAuthAuthenticationFailureHandler: OAuthAuthenticationFailureHandler,
    private val oAuthUserCustomService: OAuthUserCustomService,
    private val oAuth2AuthorizationRequestCookieRepository: OAuth2AuthorizationRequestCookieRepository,
    private val authServiceProperties: AuthServiceProperties
) {
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .authorizeHttpRequests {
            it.requestMatchers("/api/auth/members/**").hasAuthority(Role.ADMIN.authority)
            it.anyRequest().permitAll()
        }
        .headers { it.frameOptions { cfg -> cfg.sameOrigin() } }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        .exceptionHandling {
            it.accessDeniedHandler(jwtAccessDeniedHandler)
            it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        }
        .oauth2Login {
            it.authorizationEndpoint { cfg ->
                cfg.baseUri(authServiceProperties.oauth?.authorizationUri)
                cfg.authorizationRequestRepository(oAuth2AuthorizationRequestCookieRepository)
            }
            it.userInfoEndpoint { cfg -> cfg.userService(oAuthUserCustomService) }
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
        }
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration().apply {
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedOriginPatterns = listOf("*")
            allowedHeaders = listOf("*")
        }
        val corsConfigurationSource = UrlBasedCorsConfigurationSource()
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration)
        return corsConfigurationSource
    }
}