package io.brieflyz.subscription_service.config

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.config.AsyncConfig.Companion.PLATFORM_THREAD_BEAN
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch
import kotlin.test.Test

@SpringBootTest
class AsyncConfigTest {

    @Autowired
    private lateinit var asyncRunner: AsyncRunner

    @Test
    fun testAsync() {
        val size = 500
        val virtualThreadLatch = CountDownLatch(size)
        val platformThreadLatch = CountDownLatch(size)

        val startVirtual = System.nanoTime()
        for (i in 0 until size) asyncRunner.runByVirtualThread(i, virtualThreadLatch)
        virtualThreadLatch.await()
        println("Virtual thread execution time: ${(System.nanoTime() - startVirtual) / 1_000_000} ms")

        val startPlatform = System.nanoTime()
        for (i in 0 until size) asyncRunner.runByPlatformThread(i, platformThreadLatch)
        platformThreadLatch.await()
        println("Platform thread execution time: ${(System.nanoTime() - startPlatform) / 1_000_000} ms")
    }
}

@Component
class AsyncRunner {
    private val log = logger()

    @Async
    fun runByVirtualThread(id: Int, latch: CountDownLatch) {
        try {
            log.debug("Task $id running on thread ${Thread.currentThread().name}")
            Thread.sleep(100)
        } finally {
            latch.countDown()
        }
    }

    @Async(PLATFORM_THREAD_BEAN)
    fun runByPlatformThread(id: Int, latch: CountDownLatch) {
        try {
            log.debug("Task $id running on thread ${Thread.currentThread().name}")
            Thread.sleep(100)
        } finally {
            latch.countDown()
        }
    }
}