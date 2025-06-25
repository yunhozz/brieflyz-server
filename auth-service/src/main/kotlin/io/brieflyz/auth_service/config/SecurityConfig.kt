package io.brieflyz.auth_service.config

import io.brieflyz.auth_service.infra.security.jwt.JwtAccessDeniedHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtAuthenticationEntryPoint
import io.brieflyz.auth_service.infra.security.jwt.JwtFilter
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationFailureHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthAuthenticationSuccessHandler
import io.brieflyz.auth_service.infra.security.oauth.OAuthUserCustomService
import io.brieflyz.auth_service.infra.security.user.Role
import io.brieflyz.core.config.AuthServiceProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

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
        .cors { it.disable() }
        .csrf { it.disable() }
        .authorizeHttpRequests {
            it.requestMatchers(
                "/actuator/health",
                "/h2-console/**",
                "/api/auth/sign-up",
                "/api/auth/sign-in"
            ).permitAll()
            it.requestMatchers("/api/auth/members/**")
                .hasAuthority(Role.ADMIN.authority)
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
}