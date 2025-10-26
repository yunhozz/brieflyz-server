package io.brieflyz.auth_service.adapter.out.redis

import io.brieflyz.auth_service.application.port.out.CachePort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisAdapter(
    redisTemplate: RedisTemplate<String, String>
) : CachePort {

    private val ops by lazy { redisTemplate.opsForValue() }

    override fun save(key: String, value: String, ttl: Long) {
        ops.set(key, value, Duration.ofMillis(ttl))
    }

    override fun find(key: String): String? = ops.get(key)

    override fun exists(key: String): Boolean = ops.get(key) != null

    override fun delete(key: String) {
        ops.getAndDelete(key)
    }
}