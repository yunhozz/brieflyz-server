package io.brieflyz.auth_service.common.redis

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisHandler(
    redisTemplate: RedisTemplate<String, String>
) {
    private val ops by lazy { redisTemplate.opsForValue() }

    fun save(key: String, value: String, ttl: Long) {
        ops.set(key, value, Duration.ofMillis(ttl))
    }

    fun find(key: String): String = ops.get(key)
        ?: throw IllegalArgumentException("Data does not exist. Key=$key")

    fun delete(key: String) {
        ops.getAndDelete(key)
    }

    fun exists(key: String): Boolean = ops.get(key) != null
}