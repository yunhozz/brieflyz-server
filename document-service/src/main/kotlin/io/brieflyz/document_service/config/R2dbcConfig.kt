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

@Configuration
@EnableR2dbcAuditing
@EnableTransactionManagement
class R2dbcConfig {

    enum class DataSourceType {
        SOURCE,
        REPLICA
    }

    data class R2dbcConnectionProperties(
        var url: String = "",
        var username: String = "",
        var password: String = "",
        var pool: Pool? = null
    ) {
        data class Pool(
            var initialSize: Int = 0,
            var minIdle: Int = 0,
            var maxSize: Int = 0,
            var maxAcquireTime: Long = 0,
            var maxLifeTime: Long = 0
        )
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.source")
    fun sourceConnectionProperties() = R2dbcConnectionProperties()

    @Bean
    @ConfigurationProperties(prefix = "spring.r2dbc.replica")
    fun replicaConnectionProperties() = R2dbcConnectionProperties()

    @Bean
    fun sourceConnectionFactory(): ConnectionFactory = createConnectionFactory(sourceConnectionProperties())

    @Bean
    fun replicaConnectionFactory(): ConnectionFactory = createConnectionFactory(replicaConnectionProperties())

    @Bean
    @Primary
    fun routingConnectionFactory(): ConnectionFactory = object : AbstractRoutingConnectionFactory() {
        private val log = logger()

        override fun determineCurrentLookupKey(): Mono<in Any> =
            TransactionSynchronizationManager.forCurrentTransaction()
                .map { txManager ->
                    log.debug("Current transaction name: ${txManager.currentTransactionName}")
                    log.debug("Current transaction read-only: ${txManager.isCurrentTransactionReadOnly}")
                    log.debug("Actual transaction active: ${txManager.isActualTransactionActive}")

                    when (txManager.isCurrentTransactionReadOnly) {
                        true -> DataSourceType.REPLICA
                        false -> DataSourceType.SOURCE
                    } as Any
                }
                .doOnError { ex -> log.error(ex.message, ex) }
    }.apply {
        setLenientFallback(true)
        setDefaultTargetConnectionFactory(sourceConnectionFactory())
        setTargetConnectionFactories(
            mapOf(
                DataSourceType.SOURCE to sourceConnectionFactory(),
                DataSourceType.REPLICA to replicaConnectionFactory()
            )
        )
    }

    @Bean
    fun reactiveTransactionManager(): ReactiveTransactionManager =
        R2dbcTransactionManager(
            TransactionAwareConnectionFactoryProxy(routingConnectionFactory())
        )

    private fun createConnectionFactory(props: R2dbcConnectionProperties): ConnectionFactory {
        val pool = props.pool!!
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