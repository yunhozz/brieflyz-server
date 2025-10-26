package io.brieflyz.subscription_service.application.service

import io.brieflyz.core.dto.message.SubscriptionMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.application.dto.command.SaveExpiredSubscriptionsCommand
import io.brieflyz.subscription_service.application.dto.command.SendSubscriptionExpiredEventCommand
import io.brieflyz.subscription_service.application.port.`in`.CleanupExpiredSubscriptionsUseCase
import io.brieflyz.subscription_service.application.port.`in`.SaveExpiredSubscriptionsUseCase
import io.brieflyz.subscription_service.application.port.`in`.SendSubscriptionExpiredEventUseCase
import io.brieflyz.subscription_service.application.port.`in`.SoftDeleteSubscriptionsUseCase
import io.brieflyz.subscription_service.application.port.out.EmailPort
import io.brieflyz.subscription_service.application.port.out.ExpiredSubscriptionRepositoryPort
import io.brieflyz.subscription_service.application.port.out.MessagePort
import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import io.brieflyz.subscription_service.common.props.SubscriptionServiceProperties
import io.brieflyz.subscription_service.domain.model.ExpiredSubscription
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Service
class SaveExpiredSubscriptionsService(
    private val expiredSubscriptionRepositoryPort: ExpiredSubscriptionRepositoryPort
) : SaveExpiredSubscriptionsUseCase {

    private val log = logger()

    override fun saveAll(command: SaveExpiredSubscriptionsCommand) {
        val expiredSubscriptions = command.expiredSubscriptionCommands
            .map { ExpiredSubscription(it.expiredSubscriptionId, it.email, it.plan) }
        expiredSubscriptionRepositoryPort.saveAll(expiredSubscriptions)
        log.info("A total of ${expiredSubscriptions.size} subscriptions will be deleted")
    }
}

@Service
class SoftDeleteSubscriptionsService(
    private val subscriptionRepositoryPort: SubscriptionRepositoryPort
) : SoftDeleteSubscriptionsUseCase {

    private val log = logger()

    override fun deleteInIds(expiredSubscriptionIds: List<Long>) {
        subscriptionRepositoryPort.softDeleteInIdsQuery(expiredSubscriptionIds)
        log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted")
    }
}

@Service
class SendSubscriptionExpiredEventService(
    private val emailPort: EmailPort,
    private val messagePort: MessagePort,
    private val props: SubscriptionServiceProperties
) : SendSubscriptionExpiredEventUseCase {

    private val log = logger()

    companion object {
        const val EMAIL_SUBJECT = "[Brieflyz] 구독이 만료되었습니다."
        const val TEMPLATE_NAME = "subscription-expired-email"
    }

    override fun publish(command: SendSubscriptionExpiredEventCommand) {
        val now = LocalDateTime.now()
        val renewUrl = props.email.renewUrl

        val contextMap = mutableMapOf(
            "sentAt" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "expiryDate" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "renewUrl" to renewUrl,
            "supportUrl" to "",
            "unsubscribeUrl" to "",
            "year" to Year.now().toString()
        )

        val futures = mutableListOf<CompletableFuture<Boolean>>()

        command.expiredSubscriptionCommands.forEach {
            val email = it.email
            val planName = it.plan

            contextMap.putAll(mapOf("email" to email, "planName" to planName))

            val message = SubscriptionMessage(email, isCreated = false)
            messagePort.sendSubscriptionMessage(message)

            val future = emailPort.send(email, EMAIL_SUBJECT, TEMPLATE_NAME, contextMap)
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
}

@Service
class CleanupExpiredSubscriptionsService(
    private val expiredSubscriptionRepositoryPort: ExpiredSubscriptionRepositoryPort
) : CleanupExpiredSubscriptionsUseCase {

    private val log = logger()

    override fun cleanup() {
        expiredSubscriptionRepositoryPort.deleteAll()
        log.info("Clean up expired subscription table")
    }
}