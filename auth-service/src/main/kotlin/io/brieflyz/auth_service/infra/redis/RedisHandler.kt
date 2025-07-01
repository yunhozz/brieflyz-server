package io.brieflyz.auth_service.infra.redis

import io.brieflyz.auth_service.common.exception.RedisKeyNotExistsException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisHandler(
    redisTemplate: RedisTemplate<String, String>
) {
    private val ops = redisTemplate.opsForValue()

    fun save(key: String, value: String, ttl: Long) {
        ops.set(key, value, Duration.ofMillis(ttl))
    }

    fun find(key: String): String = ops.get(key)
        ?: throw RedisKeyNotExistsException("Key: $key")

    fun delete(key: String) {
        ops.getAndDelete(key)
    }

    fun exists(key: String): Boolean = ops.get(key) != null
}