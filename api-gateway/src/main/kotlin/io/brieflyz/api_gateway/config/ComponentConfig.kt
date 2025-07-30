package io.brieflyz.api_gateway.config

import io.brieflyz.core.component.JwtComponent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [JwtComponent::class])
class ComponentConfig