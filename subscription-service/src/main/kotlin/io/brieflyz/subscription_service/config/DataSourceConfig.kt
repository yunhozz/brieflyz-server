package io.brieflyz.subscription_service.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

@Configuration
@EnableJpaAuditing
class DataSourceConfig {

    private enum class DataSourceType {
        SOURCE,
        REPLICA
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.source")
    fun source(): DataSource = createDataSource()

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari.replica")
    fun replica(): DataSource = createDataSource()

    @Bean
    fun routingDataSource(): DataSource = object : AbstractRoutingDataSource() {
        override fun determineCurrentLookupKey(): DataSourceType =
            when (TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
                true -> DataSourceType.REPLICA
                false -> DataSourceType.SOURCE
            }
    }.apply {
        setDefaultTargetDataSource(source())
        setTargetDataSources(
            mapOf(
                DataSourceType.SOURCE to source(),
                DataSourceType.REPLICA to replica()
            )
        )
    }

    @Bean
    @Primary
    fun dataSourceProxy() = LazyConnectionDataSourceProxy(routingDataSource())

    private fun createDataSource(): DataSource =
        DataSourceBuilder.create()
            .type(HikariDataSource::class.java)
            .build()
}