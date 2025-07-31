package io.brieflyz.subscription_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    private fun createRedisson(config: Config.() -> Unit): RedissonClient =
        Redisson.create(Config().apply(config))

    @Bean
    @Profile("local")
    fun singleRedissonClient(): RedissonClient = createRedisson {
        useSingleServer().apply {
            address = "redis://${redisProperties.host}:${redisProperties.port}"
            connectTimeout = 100
            timeout = 3000
            retryAttempts = 3
        }
    }

    @Bean
    @Profile("dev", "prod")
    fun clusterRedissonClient(): RedissonClient = createRedisson {
        useClusterServers().apply {
            nodeAddresses = redisProperties.cluster.nodes.map { "redis://$it" }
            scanInterval = 2000
            connectTimeout = 100
            timeout = 3000
            retryAttempts = 3
        }
    }

    @Bean
    fun redisTemplate(factory: RedissonConnectionFactory): RedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        return RedisTemplate<String, String>().apply {
            connectionFactory = factory
            keySerializer = stringSerializer
            hashKeySerializer = stringSerializer
            valueSerializer = stringSerializer
            hashValueSerializer = stringSerializer
        }
    }
}