package io.brieflyz.subscription_service.adapter.`in`.scheduler.aop

import io.brieflyz.core.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.redisson.api.RedissonClient
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,
    val leaseTime: Long,
    val waitTime: Long
)

@Component
@Aspect
class DistributedLockAspect(
    private val redissonClient: RedissonClient
) {
    private val log = logger()

    @Around("@annotation(io.brieflyz.subscription_service.adapter.in.scheduler.aop.DistributedLock)")
    fun distributedLock(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val distributedLock = method.getAnnotation(DistributedLock::class.java)

        val lockKey = distributedLock.key
        val rLock = redissonClient.getLock(lockKey)

        return try {
            val lockable = rLock.tryLock(distributedLock.waitTime, distributedLock.leaseTime, TimeUnit.SECONDS)
            if (lockable) {
                log.debug("Acquire lock for key : $lockKey")
                joinPoint.proceed()
            } else {
                log.debug("Failed to acquire lock for key : $lockKey")
                null
            }

        } finally {
            try {
                log.debug("Release lock for key : $lockKey")
                rLock.unlock()
            } catch (e: Exception) {
                log.debug("Redisson lock already unlocked : $lockKey")
            }
        }
    }
}