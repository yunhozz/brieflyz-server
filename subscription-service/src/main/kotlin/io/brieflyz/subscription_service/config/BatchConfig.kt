package io.brieflyz.subscription_service.config

import io.brieflyz.subscription_service.adapter.out.batch.BatchExecutor
import io.brieflyz.subscription_service.adapter.out.persistence.entity.ExpiredSubscriptionEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.SubscriptionEntity
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val batchExecutor: BatchExecutor
) {
    companion object {
        const val CHUNK_SIZE = 100
    }

    @Bean
    fun deleteExpiredSubscriptionsJob(): Job =
        JobBuilder("deleteExpiredSubscriptionsJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(getExpiredSubscriptionIdsStep())
            .next(deleteExpiredSubscriptionListStep())
            .next(sendEmailToExpiredSubscriptionMembersStep())
            .next(cleanupExpiredSubscriptionListStep())
            .build()

    @Bean
    fun subscriptionItemReader(): ItemReader<SubscriptionEntity> =
        JpaPagingItemReaderBuilder<SubscriptionEntity>()
            .name("subscriptionItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT s FROM SubscriptionEntity s WHERE s.plan != 'UNLIMITED' AND s.deleted = false ORDER BY s.id ASC")
            .pageSize(CHUNK_SIZE)
            .build()

    @Bean
    fun getExpiredSubscriptionIdsStep(): Step =
        StepBuilder("getExpiredSubscriptionIdsStep", jobRepository)
            .chunk<SubscriptionEntity, ExpiredSubscriptionEntity>(CHUNK_SIZE, transactionManager)
            .reader(subscriptionItemReader())
            .processor { limitedSubscription ->
                val now = LocalDateTime.now()
                val updatedAt = limitedSubscription.updatedAt
                val expirationTime = limitedSubscription.plan.getExpirationTime(updatedAt!!)

                if (expirationTime <= now) {
                    ExpiredSubscriptionEntity(
                        limitedSubscription.id,
                        limitedSubscription.email,
                        limitedSubscription.plan.displayName
                    )
                } else null
            }
            .writer { chunk ->
                batchExecutor.saveExpiredSubscriptionList(chunk)
            }
            .build()

    @Bean
    fun expiredSubscriptionListItemReader(): ItemReader<ExpiredSubscriptionEntity> =
        JpaPagingItemReaderBuilder<ExpiredSubscriptionEntity>()
            .name("expiredSubscriptionListItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT es FROM ExpiredSubscriptionEntity es ORDER BY es.id ASC")
            .pageSize(CHUNK_SIZE)
            .build()

    @Bean
    fun deleteExpiredSubscriptionListStep(): Step =
        StepBuilder("deleteExpiredSubscriptionListStep", jobRepository)
            .chunk<ExpiredSubscriptionEntity, ExpiredSubscriptionEntity>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionListItemReader())
            .writer { chunk ->
                batchExecutor.softDeleteSubscriptionsInIds(chunk)
            }
            .build()

    @Bean
    fun sendEmailToExpiredSubscriptionMembersStep(): Step =
        StepBuilder("sendEmailToExpiredSubscriptionMembersStep", jobRepository)
            .chunk<ExpiredSubscriptionEntity, ExpiredSubscriptionEntity>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionListItemReader())
            .writer { chunk ->
                batchExecutor.sendEmailAndPublishEvent(chunk)
            }
            .build()

    @Bean
    fun cleanupExpiredSubscriptionListStep(): Step =
        StepBuilder("cleanupExpiredSubscriptionListStep", jobRepository)
            .tasklet({ _, _ ->
                batchExecutor.cleanupExpiredSubscriptionList()
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
}