package io.brieflyz.document_service.config

import io.brieflyz.core.annotation.JwtSubject
import io.brieflyz.core.beans.jwt.JwtManager
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class WebConfig(
    private val jwtHeaderResolver: JwtHeaderResolver
) : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(jwtHeaderResolver)
    }
}

@Component
class JwtHeaderResolver(
    private val jwtManager: JwtManager
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(JwtSubject::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<in Any> {
        val request = exchange.request
        val token = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return Mono.empty()
        val claims = jwtManager.createClaimsJws(token)?.body
        return Mono.justOrEmpty(claims?.subject)
    }
}