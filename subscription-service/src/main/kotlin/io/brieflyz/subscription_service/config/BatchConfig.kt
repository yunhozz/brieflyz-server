package io.brieflyz.subscription_service.config

import io.brieflyz.subscription_service.common.component.batch.BatchExecutionListener
import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import io.brieflyz.subscription_service.model.entity.Subscription
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

@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
    private val entityManagerFactory: EntityManagerFactory,
    private val batchExecutionListener: BatchExecutionListener
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
    fun subscriptionItemReader(): ItemReader<Subscription> =
        JpaPagingItemReaderBuilder<Subscription>()
            .name("subscriptionItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT s FROM Subscription s WHERE s.plan != 'UNLIMITED' AND s.deleted = false ORDER BY s.id ASC")
            .pageSize(CHUNK_SIZE)
            .build()

    @Bean
    fun getExpiredSubscriptionIdsStep(): Step =
        StepBuilder("getExpiredSubscriptionIdsStep", jobRepository)
            .chunk<Subscription, ExpiredSubscription>(CHUNK_SIZE, transactionManager)
            .reader(subscriptionItemReader())
            .processor { limitedSubscription ->
                if (limitedSubscription.isExpired()) {
                    ExpiredSubscription(
                        limitedSubscription.id,
                        limitedSubscription.email,
                        limitedSubscription.plan.displayName
                    )
                } else null
            }
            .writer { chunk ->
                batchExecutionListener.saveExpiredSubscriptionList(chunk)
            }
            .build()

    @Bean
    fun expiredSubscriptionListItemReader(): ItemReader<ExpiredSubscription> =
        JpaPagingItemReaderBuilder<ExpiredSubscription>()
            .name("expiredSubscriptionListItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT es FROM ExpiredSubscription es ORDER BY es.id ASC")
            .pageSize(CHUNK_SIZE)
            .build()

    @Bean
    fun deleteExpiredSubscriptionListStep(): Step =
        StepBuilder("deleteExpiredSubscriptionListStep", jobRepository)
            .chunk<ExpiredSubscription, ExpiredSubscription>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionListItemReader())
            .writer { chunk ->
                batchExecutionListener.softDeleteSubscriptionsInIds(chunk)
            }
            .build()

    @Bean
    fun sendEmailToExpiredSubscriptionMembersStep(): Step =
        StepBuilder("sendEmailToExpiredSubscriptionMembersStep", jobRepository)
            .chunk<ExpiredSubscription, ExpiredSubscription>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionListItemReader())
            .writer { chunk ->
                batchExecutionListener.sendEmail(chunk)
            }
            .build()

    @Bean
    fun cleanupExpiredSubscriptionListStep(): Step =
        StepBuilder("cleanupExpiredSubscriptionListStep", jobRepository)
            .tasklet({ _, _ ->
                batchExecutionListener.cleanupExpiredSubscriptionList()
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
}