package io.brieflyz.subscription_service.adapter.out.batch

import io.brieflyz.subscription_service.adapter.out.persistence.entity.ExpiredSubscriptionEntity
import org.springframework.batch.item.Chunk

interface BatchExecutor {
    fun saveExpiredSubscriptionList(chunk: Chunk<out ExpiredSubscriptionEntity>)
    fun softDeleteSubscriptionsInIds(chunk: Chunk<out ExpiredSubscriptionEntity>)
    fun sendEmailAndPublishEvent(chunk: Chunk<out ExpiredSubscriptionEntity>)
    fun cleanupExpiredSubscriptionList()
}