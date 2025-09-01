package io.brieflyz.api_gateway.config

import io.brieflyz.api_gateway.exception.JwtAccessDeniedHandler
import io.brieflyz.api_gateway.exception.JwtAuthenticationEntryPoint
import io.brieflyz.api_gateway.filter.JwtFilter
import io.brieflyz.core.constants.Role
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        jwtFilter: JwtFilter,
        jwtAccessDeniedHandler: JwtAccessDeniedHandler,
        jwtAuthenticationEntryPoint: JwtAuthenticationEntryPoint
    ): SecurityWebFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .addFilterBefore(jwtFilter, SecurityWebFiltersOrder.HTTP_BASIC)
        .authorizeExchange {
            it.pathMatchers("/favicon.ico", "/health", "/actuator/**").permitAll()
            it.pathMatchers("/api/auth/**", "/oauth2/authorization/**", "/login/oauth2/code/**").permitAll()
            it.pathMatchers("/api/admin/**").hasAuthority(Role.ADMIN.name)
            it.pathMatchers(HttpMethod.GET, "/api/members/**").hasAuthority(Role.ADMIN.name)
            it.pathMatchers("/api/subscriptions/**").hasAuthority(Role.USER.name)
            it.pathMatchers("/api/documents/**").hasAuthority(Role.MEMBER.name)
            it.anyExchange().authenticated()
        }
        .exceptionHandling {
            it.accessDeniedHandler(jwtAccessDeniedHandler)
            it.authenticationEntryPoint(jwtAuthenticationEntryPoint)
        }
        .build()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedOriginPatterns = listOf("*")
            allowedHeaders = listOf("*")
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}