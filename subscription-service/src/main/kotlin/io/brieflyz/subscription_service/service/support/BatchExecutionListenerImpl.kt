package io.brieflyz.subscription_service.service.support

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import io.brieflyz.subscription_service.repository.ExpiredSubscriptionRepository
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import io.brieflyz.subscription_service.service.MailService
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Component
class BatchExecutionListenerImpl(
    private val subscriptionRepository: SubscriptionRepository,
    private val expiredSubscriptionRepository: ExpiredSubscriptionRepository,
    private val mailService: MailService
) : BatchExecutionListener {

    private val log = logger()

    override fun saveExpiredSubscriptionList(chunk: Chunk<out ExpiredSubscription>) {
        expiredSubscriptionRepository.saveAll(chunk.toList())
        log.info("A total of ${chunk.size()} subscriptions will be deleted")
    }

    override fun softDeleteSubscriptionsInIds(chunk: Chunk<out ExpiredSubscription>) {
        val expiredSubscriptionIds = chunk.map { it.id }
        subscriptionRepository.softDeleteSubscriptionsInIdsQuery(expiredSubscriptionIds)
        log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted")
    }

    override fun sendEmail(chunk: Chunk<out ExpiredSubscription>) {
        val now = LocalDateTime.now()
        val context = Context().apply {
            setVariable("sentAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            setVariable("expiryDate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            setVariable("renewUrl", "")
            setVariable("supportUrl", "")
            setVariable("unsubscribeUrl", "")
            setVariable("year", Year.now().toString())
        }
        // TODO: 구독 갱신, 고객 지원, 구독 취소 URL 생성

        val futures = mutableListOf<CompletableFuture<Boolean>>()

        chunk.forEach { expiredSubscription ->
            val email = expiredSubscription.email
            val planName = expiredSubscription.plan

            context.setVariable("email", email)
            context.setVariable("planName", planName)

            val future = mailService.sendAsync(email, context)
            futures.add(future)
        }

        CompletableFuture.allOf(*futures.toTypedArray()).join()

        futures.forEach { future ->
            try {
                if (!future.get()) log.warn("Failed to send mail")
            } catch (e: Exception) {
                log.error("Mail send exception occurred: ${e.message}", e)
            }
        }
    }

    override fun cleanupExpiredSubscriptionList() {
        expiredSubscriptionRepository.deleteAllInBatch()
        log.info("Clean up expired subscription table")
    }
}