package io.brieflyz.subscription_service.service.support

import io.brieflyz.subscription_service.service.MailService
import io.brieflyz.subscription_service.service.SubscriptionService
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class BatchExecutionListenerImpl(
    private val subscriptionService: SubscriptionService,
    private val mailService: MailService
) : BatchExecutionListener {

    override fun softDeleteSubscriptionsInIds(chunk: Chunk<out Long>) =
        subscriptionService.softDeleteSubscriptionsInIds(chunk.toList())

    override fun sendEmail(chunk: Chunk<out Map<String, String>>) {
        chunk.forEach { entry ->
            entry.forEach { (email, plan) ->
                mailService.sendAsync(email, plan)
            }
        }
    }
}