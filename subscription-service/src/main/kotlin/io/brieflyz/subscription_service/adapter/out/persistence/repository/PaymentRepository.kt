package io.brieflyz.subscription_service.adapter.out.persistence.repository

import io.brieflyz.subscription_service.adapter.out.persistence.entity.PaymentEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findAllBySubscription(subscriptionEntity: SubscriptionEntity): List<PaymentEntity>
}