package io.brieflyz.subscription_service.config

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.model.entity.Subscription
import io.brieflyz.subscription_service.service.support.BatchExecutionListener
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.JobSynchronizationManager
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.support.ListItemReader
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
    private val log = logger()

    companion object {
        const val CHUNK_SIZE = 100
        const val EXPIRED_SUBSCRIPTION_IDS = "expiredSubscriptionIds"
        const val EXPIRED_SUBSCRIPTION_EMAIL_PLAN_MAP_LIST = "expiredSubscriptionEmailPlanMapList"
    }

    @Bean
    fun deleteExpiredSubscriptionsJob(): Job =
        JobBuilder("deleteExpiredSubscriptionsJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(getExpiredSubscriptionIdsStep())
            .next(deleteExpiredSubscriptionsStep())
            .next(sendEmailToExpiredSubscriptionMembersStep())
            .next(logDeletionResultStep())
            .build()

    @Bean
    fun subscriptionItemReader(): ItemReader<Subscription> =
        JpaPagingItemReaderBuilder<Subscription>()
            .name("subscriptionItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("select s from Subscription s where s.plan != 'UNLIMITED' and s.deleted = false order by s.id asc")
            .pageSize(CHUNK_SIZE)
            .build()

    private data class SubscriptionChunk(
        val id: Long,
        val email: String,
        val plan: SubscriptionPlan
    )

    @Bean
    fun getExpiredSubscriptionIdsStep(): Step =
        StepBuilder("getExpiredSubscriptionIdsStep", jobRepository)
            .chunk<Subscription, SubscriptionChunk>(CHUNK_SIZE, transactionManager)
            .reader(subscriptionItemReader())
            .processor { limitedSubscription ->
                if (limitedSubscription.isExpired()) {
                    SubscriptionChunk(
                        limitedSubscription.id,
                        limitedSubscription.email,
                        limitedSubscription.plan
                    )
                } else null
            }
            .writer { chunk ->
                val executionContext = JobSynchronizationManager.getContext()!!.jobExecution.executionContext
                val expiredSubscriptionIds =
                    executionContext.get(EXPIRED_SUBSCRIPTION_IDS) as? MutableList<Long>
                        ?: mutableListOf()
                val expiredSubscriptionEmailPlanMapList =
                    executionContext.get(EXPIRED_SUBSCRIPTION_EMAIL_PLAN_MAP_LIST) as? MutableList<Map<String, String>>
                        ?: mutableListOf()

                chunk.forEach { (id, email, plan) ->
                    expiredSubscriptionIds.add(id)
                    expiredSubscriptionEmailPlanMapList.add(mapOf(email to plan.name))
                }

                executionContext.put(EXPIRED_SUBSCRIPTION_IDS, expiredSubscriptionIds)
                executionContext.put(EXPIRED_SUBSCRIPTION_EMAIL_PLAN_MAP_LIST, expiredSubscriptionEmailPlanMapList)
            }
            .build()

    @Bean
    @StepScope
    fun expiredSubscriptionIdsItemReader(): ItemReader<Long> {
        val executionContext = JobSynchronizationManager.getContext()!!.jobExecution.executionContext
        val expiredSubscriptionIds = executionContext[EXPIRED_SUBSCRIPTION_IDS] as? List<Long> ?: emptyList()
        return ListItemReader(expiredSubscriptionIds)
    }

    @Bean
    fun deleteExpiredSubscriptionsStep(): Step =
        StepBuilder("deleteExpiredSubscriptionsStep", jobRepository)
            .chunk<Long, Long>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionIdsItemReader())
            .writer { chunk ->
                batchExecutionListener.softDeleteSubscriptionsInIds(chunk)
            }
            .build()

    @Bean
    @StepScope
    fun expiredSubscriptionEmailListItemReader(): ItemReader<Map<String, String>> {
        val executionContext = JobSynchronizationManager.getContext()!!.jobExecution.executionContext
        val expiredSubscriptionEmailPlanMap =
            executionContext.get(EXPIRED_SUBSCRIPTION_EMAIL_PLAN_MAP_LIST) as? MutableList<Map<String, String>>
                ?: mutableListOf()

        return ListItemReader(expiredSubscriptionEmailPlanMap)
    }

    @Bean
    fun sendEmailToExpiredSubscriptionMembersStep(): Step =
        StepBuilder("sendEmailToExpiredSubscriptionMembersStep", jobRepository)
            .chunk<Map<String, String>, Map<String, String>>(CHUNK_SIZE, transactionManager)
            .reader(expiredSubscriptionEmailListItemReader())
            .writer { chunk ->
                batchExecutionListener.sendEmail(chunk)
            }
            .build()

    @Bean
    fun logDeletionResultStep(): Step =
        StepBuilder("logDeletionResultStep", jobRepository)
            .tasklet({ _, chunkContext ->
                val executionContext = chunkContext.stepContext.jobExecutionContext
                val expiredSubscriptionIds = executionContext[EXPIRED_SUBSCRIPTION_IDS] as? List<Long> ?: emptyList()
                log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted.")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
}