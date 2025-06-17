package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.infra.security.jwt.JwtAccessDeniedHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtAuthenticationEntryPoint
import io.brieflyz.auth_service.infra.security.jwt.JwtFilter
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
    private val authServiceProperties: AuthServiceProperties
) {
    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() } // TODO: CSRF 설정
        .authorizeHttpRequests {
            it.requestMatchers("/actuator/health", "/h2-console/**", "/api/auth/login").permitAll()
            it.anyRequest().authenticated()
        }
        .headers { it.frameOptions { cfg -> cfg.sameOrigin() } }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java)
        .oauth2Login {
            val oauth = authServiceProperties.oauth
            it.successHandler(oAuthAuthenticationSuccessHandler)
            it.failureHandler(oAuthAuthenticationFailureHandler)
            it.authorizationEndpoint { cfg -> cfg.baseUri(oauth?.authorizationUri) }
            it.redirectionEndpoint { cfg -> cfg.baseUri(oauth?.redirectUri) }
            it.userInfoEndpoint { cfg -> cfg.userService(oAuthUserCustomService) }
        }
        .exceptionHandling {
            it.accessDeniedHandler(jwtAccessDeniedHandler)
            it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        }
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val corsConfiguration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", corsConfiguration)
        }
    }
}