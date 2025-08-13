package io.brieflyz.subscription_service.config

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.JobSynchronizationManager
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchConfig(
    private val jobRepository: JobRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val transactionManager: PlatformTransactionManager
) {
    private val log = logger()

    @Bean
    fun deleteExpiredSubscriptionsJob(): Job =
        JobBuilder("deleteExpiredSubscriptionsJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(getExpiredSubscriptionIdsStep()).on("NOOP").to(logDeletionResultStep())
            .from(getExpiredSubscriptionIdsStep()).on("*").to(deleteExpiredSubscriptionsStep())
            .next(logDeletionResultStep())
            .end()
            .build()

    @Bean
    fun getExpiredSubscriptionIdsStep(): Step =
        StepBuilder("getExpiredSubscriptionIdsStep", jobRepository)
            .tasklet({ contribution, chunkContext ->
                val expiredSubscriptionIds = subscriptionRepository.findLimitedSubscriptionsQuery()
                    .filter { it.isExpired() }
                    .map { it.id }

                if (expiredSubscriptionIds.isEmpty()) {
                    contribution.exitStatus = ExitStatus.NOOP
                }

                val jobExecutionContext = chunkContext.stepContext.stepExecution.jobExecution.executionContext
                jobExecutionContext.put("expiredSubscriptionIds", expiredSubscriptionIds)
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()

    @Bean
    fun deleteExpiredSubscriptionsStep(): Step =
        StepBuilder("deleteExpiredSubscriptionsStep", jobRepository)
            .chunk<Long, Long>(100, transactionManager)
            .reader(expiredSubscriptionIdsReader())
            .writer(expiredSubscriptionIdsWriter())
            .build()

    @Bean
    fun expiredSubscriptionIdsReader(): ItemReader<Long> = ItemReader {
        val jobExecution = JobSynchronizationManager.getContext()?.jobExecution
        val jobExecutionContext = jobExecution?.executionContext
        val expiredSubscriptionIds = jobExecutionContext?.get("expiredSubscriptionIds") as? MutableList<Long>

        if (expiredSubscriptionIds.isNullOrEmpty()) null
        else expiredSubscriptionIds.removeAt(0)
    }

    @Bean
    fun expiredSubscriptionIdsWriter(): ItemWriter<Long> = ItemWriter { items ->
        subscriptionRepository.softDeleteSubscriptionsInIdsQuery(items.toList())
    }

    @Bean
    fun logDeletionResultStep(): Step =
        StepBuilder("logDeletionResultStep", jobRepository)
            .tasklet({ contribution, chunkContext ->
                val jobExecutionContext = chunkContext.stepContext.stepExecution.jobExecution.executionContext
                val expiredSubscriptionIds = jobExecutionContext.get("expiredSubscriptionIds") as List<Long>
                log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted.")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
}