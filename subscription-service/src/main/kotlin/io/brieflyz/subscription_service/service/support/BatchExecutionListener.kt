package io.brieflyz.subscription_service.service.support

import io.brieflyz.subscription_service.model.entity.ExpiredSubscription
import org.springframework.batch.item.Chunk

interface BatchExecutionListener {
    fun saveExpiredSubscriptionList(chunk: Chunk<out ExpiredSubscription>)
    fun softDeleteSubscriptionsInIds(chunk: Chunk<out ExpiredSubscription>)
    fun sendEmail(chunk: Chunk<out ExpiredSubscription>)
    fun cleanupExpiredSubscriptionList()
}