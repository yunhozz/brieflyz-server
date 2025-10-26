package io.brieflyz.document_service.config

import io.brieflyz.core.utils.logger
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy
import org.springframework.r2dbc.connection.lookup.AbstractRoutingConnectionFactory
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionSynchronizationManager
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@Configuration
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig {

    data class R2dbcProperties(
        var source: ConnectionProperties = ConnectionProperties(),
        var replicas: List<ConnectionProperties> = listOf(ConnectionProperties())
    ) {
        data class ConnectionProperties(
            var url: String = "",
            var username: String = "",
            var password: String = "",
            var pool: Pool = Pool()
        ) {
            data class Pool(
                var initialSize: Int = 0,
                var minIdle: Int = 0,
                var maxSize: Int = 0,
                var maxAcquireTime: Long = 0,
                var maxLifeTime: Long = 0
            )
        }
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc")
    fun r2dbcProperties() = R2dbcProperties()

    @Bean
    fun sourceConnectionFactory(): ConnectionFactory = createConnectionFactory(r2dbcProperties().source)

    @Bean
    fun replicaConnectionFactories(): List<ConnectionFactory> =
        r2dbcProperties().replicas.map { createConnectionFactory(it) }

    @Bean
    @Primary
    fun routingConnectionFactory(): ConnectionFactory = object : AbstractRoutingConnectionFactory() {
        private val log = logger()
        private var replicaIndex = AtomicInteger(0)

        override fun determineCurrentLookupKey(): Mono<in Any> =
            TransactionSynchronizationManager.forCurrentTransaction()
                .map { txManager ->
                    log.debug("Current transaction name: ${txManager.currentTransactionName}")
                    log.debug("Current transaction read-only: ${txManager.isCurrentTransactionReadOnly}")
                    log.debug("Actual transaction active: ${txManager.isActualTransactionActive}")

                    if (txManager.isCurrentTransactionReadOnly) {
                        val replicas = replicaConnectionFactories()
                        replicas[Math.floorMod(replicaIndex.getAndIncrement(), replicas.size)] // round-robin
                    } else {
                        sourceConnectionFactory()
                    } as Any
                }
                .doOnError { ex -> log.error(ex.message, ex) }
    }.apply {
        setLenientFallback(true)
        setDefaultTargetConnectionFactory(sourceConnectionFactory())
        setTargetConnectionFactories(
            mapOf(sourceConnectionFactory() to sourceConnectionFactory())
                    + replicaConnectionFactories().associateWith { it }
        )
    }

    @Bean
    fun reactiveTransactionManager(): ReactiveTransactionManager =
        R2dbcTransactionManager(
            TransactionAwareConnectionFactoryProxy(routingConnectionFactory())
        )

    private fun createConnectionFactory(props: R2dbcProperties.ConnectionProperties): ConnectionFactory {
        val pool = props.pool
        val connectionFactory = ConnectionFactoryBuilder.withUrl(props.url)
            .username(props.username)
            .password(props.password)
            .build()
        val connectionPoolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
            .initialSize(pool.initialSize)
            .minIdle(pool.minIdle)
            .maxSize(pool.maxSize)
            .maxAcquireTime(Duration.ofMillis(pool.maxAcquireTime))
            .maxLifeTime(Duration.ofMillis(pool.maxLifeTime))
            .build()

        val connectionPool = ConnectionPool(connectionPoolConfig)
        connectionPool.warmup().subscribe()

        return connectionPool
    }
}