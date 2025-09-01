package io.brieflyz.subscription_service.service

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.SubscriptionMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.config.SubscriptionServiceProperties
import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import io.brieflyz.subscription_service.repository.ExpiredSubscriptionRepository
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import io.brieflyz.subscription_service.service.support.MailProducer
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Service
class BatchExecutionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val expiredSubscriptionRepository: ExpiredSubscriptionRepository,
    private val mailProducer: MailProducer,
    private val kafkaSender: KafkaSender,
    private val subscriptionServiceProperties: SubscriptionServiceProperties
) {
    private val log = logger()

    companion object {
        const val EMAIL_SUBJECT = "[Brieflyz] 구독 만료 안내 메일입니다."
        const val TEMPLATE_NAME = "subscription-expired-email"
    }

    fun saveExpiredSubscriptionList(expiredSubscriptionList: List<ExpiredSubscription>) {
        expiredSubscriptionRepository.saveAll(expiredSubscriptionList)
        log.info("A total of ${expiredSubscriptionList.size} subscriptions will be deleted")
    }

    fun softDeleteSubscriptionsInIds(expiredSubscriptionIds: List<Long>) {
        subscriptionRepository.softDeleteSubscriptionsInIdsQuery(expiredSubscriptionIds)
        log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted")
    }

    fun sendEmailAndPublishEvent(expiredSubscriptionList: List<ExpiredSubscription>) {
        val now = LocalDateTime.now()
        val renewUrl = subscriptionServiceProperties.email.renewUrl

        val context = Context().apply {
            setVariable("sentAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            setVariable("expiryDate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            setVariable("renewUrl", renewUrl)
            setVariable("supportUrl", "")
            setVariable("unsubscribeUrl", "")
            setVariable("year", Year.now().toString())
        }

        val futures = mutableListOf<CompletableFuture<Boolean>>()

        expiredSubscriptionList.forEach {
            val email = it.email
            val planName = it.plan

            context.setVariable("email", email)
            context.setVariable("planName", planName)

            val future = mailProducer.sendAsync(email, EMAIL_SUBJECT, TEMPLATE_NAME, context)

            val message = SubscriptionMessage(email, isCreated = false)
            kafkaSender.send(KafkaTopic.SUBSCRIPTION_TOPIC, message)

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

    fun cleanupExpiredSubscriptionList() {
        expiredSubscriptionRepository.deleteAllInBatch()
        log.info("Clean up expired subscription table")
    }
}