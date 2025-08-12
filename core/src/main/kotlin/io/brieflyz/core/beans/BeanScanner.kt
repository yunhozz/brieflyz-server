package io.brieflyz.core.beans

import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

private const val COMMON_BEANS_DIR = "io.brieflyz.core.beans"
private const val JWT_BEANS_DIR = "io.brieflyz.core.beans.jwt"
private const val KAFKA_BEANS_DIR = "io.brieflyz.core.beans.kafka"
private const val MAPPER_BEANS_DIR = "io.brieflyz.core.beans.mapper"

@Configuration
@ConfigurationPropertiesScan(basePackages = [COMMON_BEANS_DIR])
@ComponentScan(basePackages = [COMMON_BEANS_DIR])
class CommonBeanScanner

@Configuration
@ConfigurationPropertiesScan(basePackages = [JWT_BEANS_DIR])
@ComponentScan(basePackages = [JWT_BEANS_DIR])
class JwtBeanScanner

@Configuration
@ComponentScan(basePackages = [KAFKA_BEANS_DIR])
class KafkaBeanScanner

@Configuration
@ComponentScan(basePackages = [MAPPER_BEANS_DIR])
class MapperBeanScanner