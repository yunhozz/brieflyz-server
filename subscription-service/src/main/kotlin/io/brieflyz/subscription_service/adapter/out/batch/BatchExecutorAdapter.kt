package io.brieflyz.subscription_service.adapter.out.batch

import io.brieflyz.subscription_service.adapter.out.persistence.entity.ExpiredSubscriptionEntity
import io.brieflyz.subscription_service.application.dto.command.ExpiredSubscriptionCommand
import io.brieflyz.subscription_service.application.dto.command.SaveExpiredSubscriptionsCommand
import io.brieflyz.subscription_service.application.dto.command.SendSubscriptionExpiredEventCommand
import io.brieflyz.subscription_service.application.port.`in`.CleanupExpiredSubscriptionsUseCase
import io.brieflyz.subscription_service.application.port.`in`.SaveExpiredSubscriptionsUseCase
import io.brieflyz.subscription_service.application.port.`in`.SendSubscriptionExpiredEventUseCase
import io.brieflyz.subscription_service.application.port.`in`.SoftDeleteSubscriptionsUseCase
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class BatchExecutorAdapter(
    private val saveExpiredSubscriptionsUseCase: SaveExpiredSubscriptionsUseCase,
    private val softDeleteSubscriptionsUseCase: SoftDeleteSubscriptionsUseCase,
    private val sendSubscriptionExpiredEventUseCase: SendSubscriptionExpiredEventUseCase,
    private val cleanupExpiredSubscriptionsUseCase: CleanupExpiredSubscriptionsUseCase
) : BatchExecutor {

    override fun saveExpiredSubscriptionList(chunk: Chunk<out ExpiredSubscriptionEntity>) {
        val commands = chunk.toList().map { expiredSubscription ->
            ExpiredSubscriptionCommand(
                expiredSubscription.id,
                expiredSubscription.email,
                expiredSubscription.plan
            )
        }
        saveExpiredSubscriptionsUseCase.saveAll(SaveExpiredSubscriptionsCommand(commands))
    }

    override fun softDeleteSubscriptionsInIds(chunk: Chunk<out ExpiredSubscriptionEntity>) {
        val expiredSubscriptionIds = chunk.map { it.id }
        softDeleteSubscriptionsUseCase.deleteInIds(expiredSubscriptionIds)
    }

    override fun sendEmailAndPublishEvent(chunk: Chunk<out ExpiredSubscriptionEntity>) {
        val commands = chunk.toList().map { expiredSubscription ->
            ExpiredSubscriptionCommand(
                expiredSubscription.id,
                expiredSubscription.email,
                expiredSubscription.plan
            )
        }
        sendSubscriptionExpiredEventUseCase.publish(SendSubscriptionExpiredEventCommand(commands))
    }

    override fun cleanupExpiredSubscriptionList() {
        cleanupExpiredSubscriptionsUseCase.cleanup()
    }
}