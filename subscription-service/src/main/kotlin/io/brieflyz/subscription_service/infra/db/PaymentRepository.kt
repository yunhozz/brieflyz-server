package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findAllBySubscription(subscription: Subscription): List<Payment>
}