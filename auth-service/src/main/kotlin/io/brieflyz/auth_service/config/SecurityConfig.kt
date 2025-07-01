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
//    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
//    private val jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint,
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
//        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .headers { it.frameOptions { cfg -> cfg.sameOrigin() } }
//        .formLogin { it.disable() }
//        .httpBasic { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
//        .authorizeHttpRequests {
//            it.requestMatchers(HttpMethod.GET, "/api/members/**").hasAuthority(Role.ADMIN.authority)
//            it.anyRequest().permitAll()
//        }
        .userDetailsService(userDetailsService)
//        .exceptionHandling {
//            it.accessDeniedHandler(jwtAccessDeniedHandler)
//            it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
//        }
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

//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val corsConfiguration = CorsConfiguration().apply {
//            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
//            allowedOriginPatterns = listOf("*")
//            allowedHeaders = listOf("*")
//        }
//        return UrlBasedCorsConfigurationSource().apply {
//            registerCorsConfiguration("/**", corsConfiguration)
//        }
//    }
}