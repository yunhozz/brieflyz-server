package io.brieflyz.subscription_service.adapter.out.persistence.repository

import io.brieflyz.subscription_service.adapter.out.persistence.entity.PaymentEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.SubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PaymentRepository : JpaRepository<PaymentEntity, Long> {
    fun findAllBySubscription(subscriptionEntity: SubscriptionEntity): List<PaymentEntity>

    @Query("select p from PaymentEntity p join p.subscription s where s.id = :subscriptionId")
    fun findAllBySubscriptionId(subscriptionId: Long): List<PaymentEntity>
}