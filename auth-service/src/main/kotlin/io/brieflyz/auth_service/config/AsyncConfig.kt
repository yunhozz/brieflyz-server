package io.brieflyz.auth_service.config

import io.brieflyz.core.utils.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    private val log = logger()

    companion object {
        const val THREAD_COUNT = 8
        const val THREAD_QUEUE_CAPACITY = 500
        const val PLATFORM_THREAD_BEAN = "platform-thread-bean"
    }

    override fun getAsyncExecutor(): Executor? {
        val factory = Thread.ofVirtual()
            .name("auth-service-virtual-thread-", 0)
            .uncaughtExceptionHandler { t, ex ->
                log.error("Uncaught exception in thread ${t.name}: ${ex.message}", ex)
            }
            .factory()

        return Executors.newThreadPerTaskExecutor(factory)
    }

    @Bean(name = [PLATFORM_THREAD_BEAN])
    fun platformThreadExecutor(): Executor? {
        val factory = Thread.ofPlatform()
            .name("auth-service-platform-thread-", 0)
            .uncaughtExceptionHandler { t, ex ->
                log.error("Uncaught exception in thread ${t.name}: ${ex.message}", ex)
            }
            .factory()

        return ThreadPoolTaskExecutor().apply {
            corePoolSize = THREAD_COUNT * 2
            maxPoolSize = THREAD_COUNT * 4
            queueCapacity = THREAD_QUEUE_CAPACITY
            setThreadFactory(factory)
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }
    }
}