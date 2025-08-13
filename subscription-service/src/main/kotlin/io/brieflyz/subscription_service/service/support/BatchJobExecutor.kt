package io.brieflyz.subscription_service.service.support

import io.brieflyz.subscription_service.common.annotation.DistributedLock
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameter
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@EnableScheduling
class BatchJobExecutor(
    private val jobLauncher: JobLauncher,
    private val deleteExpiredSubscriptionsJob: Job
) {
    @Scheduled(cron = "0 0 0 * * *")
    @DistributedLock(key = "deleteExpiredSubscriptionsJobKey", leaseTime = 60, waitTime = 0)
    fun deleteExpiredSubscriptionsEveryDay(): JobExecution =
        jobLauncher.run(deleteExpiredSubscriptionsJob, createDefaultJobParameters())

    private fun createDefaultJobParameters(): JobParameters = JobParametersBuilder()
        .addJobParameters(
            JobParameters(
                mapOf(
                    "uuid" to JobParameter(UUID.randomUUID().toString(), String::class.java),
                    "time" to JobParameter(System.currentTimeMillis(), Long::class.java)
                )
            )
        )
        .toJobParameters()
}