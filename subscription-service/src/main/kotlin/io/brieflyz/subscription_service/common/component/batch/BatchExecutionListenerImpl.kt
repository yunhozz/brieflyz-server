package io.brieflyz.subscription_service.common.component.batch

import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import io.brieflyz.subscription_service.service.BatchExecutionService
import org.springframework.batch.item.Chunk
import org.springframework.stereotype.Component

@Component
class BatchExecutionListenerImpl(
    private val batchExecutionService: BatchExecutionService
) : BatchExecutionListener {

    override fun saveExpiredSubscriptionList(chunk: Chunk<out ExpiredSubscription>) {
        val expiredSubscriptionList = chunk.toList()
        batchExecutionService.saveExpiredSubscriptionList(expiredSubscriptionList)
    }

    override fun softDeleteSubscriptionsInIds(chunk: Chunk<out ExpiredSubscription>) {
        val expiredSubscriptionIds = chunk.map { it.id }
        batchExecutionService.softDeleteSubscriptionsInIds(expiredSubscriptionIds)
    }

    override fun sendEmail(chunk: Chunk<out ExpiredSubscription>) {
        val expiredSubscriptionList = chunk.toList()
        batchExecutionService.sendEmail(expiredSubscriptionList)
    }

    override fun cleanupExpiredSubscriptionList() {
        batchExecutionService.cleanupExpiredSubscriptionList()
    }
}