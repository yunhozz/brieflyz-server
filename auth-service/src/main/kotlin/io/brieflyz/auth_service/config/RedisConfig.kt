package io.brieflyz.auth_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties
) {
    private fun createRedisson(config: Config.() -> Unit): RedissonClient =
        Redisson.create(Config().apply(config))

    @Bean
    fun clusterRedissonClient(): RedissonClient = createRedisson {
        val nodes = redisProperties.cluster.nodes
        if (nodes.size == 1) {
            useSingleServer().apply {
                address = "redis://${nodes.first()}"
                connectTimeout = 100
                timeout = 3000
                retryAttempts = 3
            }
        } else {
            useClusterServers().apply {
                nodeAddresses = nodes.map { "redis://$it" }
                scanInterval = 2000
                connectTimeout = 100
                timeout = 3000
                retryAttempts = 3
            }
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