package io.brieflyz.subscription_service.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "expired_subscription")
class ExpiredSubscriptionEntity(
    @Id
    val id: Long,
    val email: String,
    val plan: String
)