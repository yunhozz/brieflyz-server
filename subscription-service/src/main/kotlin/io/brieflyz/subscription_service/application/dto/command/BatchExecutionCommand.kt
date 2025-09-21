package io.brieflyz.subscription_service.application.dto.command

data class ExpiredSubscriptionCommand(
    val expiredSubscriptionId: Long,
    val email: String,
    val plan: String
)

data class SaveExpiredSubscriptionsCommand(
    val expiredSubscriptionCommands: List<ExpiredSubscriptionCommand>
)

data class SendSubscriptionExpiredEventCommand(
    val expiredSubscriptionCommands: List<ExpiredSubscriptionCommand>
)