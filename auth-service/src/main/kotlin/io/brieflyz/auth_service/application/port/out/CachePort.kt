package io.brieflyz.auth_service.application.port.out

interface CachePort {
    fun save(key: String, value: String, ttl: Long)
    fun find(key: String): String?
    fun exists(key: String): Boolean
    fun delete(key: String)
}