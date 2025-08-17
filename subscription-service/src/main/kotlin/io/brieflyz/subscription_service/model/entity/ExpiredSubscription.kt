package io.brieflyz.subscription_service.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class ExpiredSubscription(
    @Id
    val id: Long,
    val email: String,
    val plan: String
)