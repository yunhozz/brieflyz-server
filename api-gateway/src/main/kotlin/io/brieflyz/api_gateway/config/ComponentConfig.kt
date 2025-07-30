package io.brieflyz.api_gateway.config

import io.brieflyz.core.component.JwtManager
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [JwtManager::class])
class ComponentConfig