package io.brieflyz.auth_service.config

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
    @Bean
    @Profile("local")
    fun singleRedissonClient(): RedissonClient = Redisson.create(
        Config().also { config ->
            config.useSingleServer()
                .setAddress("redis://${redisProperties.host}:${redisProperties.port}")
                .setConnectTimeout(100)
                .setTimeout(3000)
                .setRetryAttempts(3)
        }
    )

    @Bean
    @Profile("dev", "prod")
    fun clusterRedissonClient(): RedissonClient = Redisson.create(
        Config().also { config ->
            val nodes = redisProperties.cluster.nodes
            val addresses = nodes.map { "redis://$it" }.toTypedArray()
            config.useClusterServers()
                .addNodeAddress(*addresses)
                .setScanInterval(2000)
                .setConnectTimeout(100)
                .setTimeout(3000)
                .setRetryAttempts(3)
        }
    )

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