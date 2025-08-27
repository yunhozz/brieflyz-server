package io.brieflyz.document_service.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class ReactiveRedisConfig(
    private val redisProperties: RedisProperties
) {
    private fun createRedisson(config: Config.() -> Unit): RedissonClient =
        Redisson.create(Config().apply(config))

    @Bean
    fun redissonClient(): RedissonClient = createRedisson {
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
    fun reactiveRedisTemplate(factory: RedissonConnectionFactory): ReactiveRedisTemplate<String, String> {
        val stringSerializer = StringRedisSerializer()
        val redisSerializationContext = RedisSerializationContext.newSerializationContext<String, String>()
            .key(stringSerializer)
            .value(stringSerializer)
            .hashKey(stringSerializer)
            .hashValue(stringSerializer)
            .build()

        return ReactiveRedisTemplate(factory, redisSerializationContext)
    }
}