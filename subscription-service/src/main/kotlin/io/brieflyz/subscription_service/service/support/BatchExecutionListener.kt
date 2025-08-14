package io.brieflyz.subscription_service.service.support

import org.springframework.batch.item.Chunk

interface BatchExecutionListener {
    fun softDeleteSubscriptionsInIds(chunk: Chunk<out Long>)
    fun sendEmail(chunk: Chunk<out Map<String, String>>)
}