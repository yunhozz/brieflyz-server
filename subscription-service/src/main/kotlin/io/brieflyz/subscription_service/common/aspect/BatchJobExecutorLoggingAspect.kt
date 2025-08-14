package io.brieflyz.subscription_service.common.aspect

import io.brieflyz.core.utils.logger
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.batch.core.JobExecution
import org.springframework.stereotype.Component

@Component
@Aspect
class BatchJobExecutorLoggingAspect {

    private val log = logger()

    @Around("execution(* io.brieflyz.subscription_service.service.BatchService.*(..))")
    fun logBatchJobExecutor(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val batchJobName = signature.name

        log.info(">>>>> Batch job $batchJobName started")

        return try {
            val result = joinPoint.proceed()
            log.info("<<<<< Batch job $batchJobName completed successfully")

            if (result is JobExecution) {
                log.info(createLogMessage(result))
            }

            result

        } catch (e: Exception) {
            log.error("<<<<< Failed to run scheduler: $batchJobName", e)
            null
        }
    }

    private fun createLogMessage(jobExecution: JobExecution) = buildString {
        appendLine("\n[Job Execution Info]")
        appendLine("Job Id: ${jobExecution.jobId}")
        appendLine("Job Name: ${jobExecution.jobInstance.jobName}")
        appendLine("Job Instance: ${jobExecution.jobInstance}")
        appendLine("Job Step Executions: ${jobExecution.stepExecutions}")
        appendLine("Job Status: ${jobExecution.status}")
        appendLine("Job Exit Status: ${jobExecution.exitStatus}")
        appendLine("Job Last Updated: ${jobExecution.lastUpdated}")
        appendLine("Job Failure Exceptions: ${jobExecution.failureExceptions}")
    }
}