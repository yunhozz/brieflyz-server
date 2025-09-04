package io.brieflyz.subscription_service.application.port.`in`

import io.brieflyz.subscription_service.application.dto.command.SaveExpiredSubscriptionsCommand
import io.brieflyz.subscription_service.application.dto.command.SendSubscriptionExpiredEventCommand

interface SaveExpiredSubscriptionsUseCase {
    fun saveAll(command: SaveExpiredSubscriptionsCommand)
}

interface SoftDeleteSubscriptionsUseCase {
    fun deleteInIds(expiredSubscriptionIds: List<Long>)
}

interface SendSubscriptionExpiredEventUseCase {
    fun publish(command: SendSubscriptionExpiredEventCommand)
}

interface CleanupExpiredSubscriptionsUseCase {
    fun cleanup()
}