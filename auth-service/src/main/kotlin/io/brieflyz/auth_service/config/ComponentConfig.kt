package io.brieflyz.auth_service.config

import io.brieflyz.core.component.JwtComponent
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackageClasses = [JwtComponent::class])
class ComponentConfig