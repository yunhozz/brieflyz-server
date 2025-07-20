package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PaymentRepository : JpaRepository<Payment, Long> {
    @Query("select p from Payment p join fetch p.subscription s where s.id = :id")
    fun findWithSubscriptionById(id: Long): Payment?
}