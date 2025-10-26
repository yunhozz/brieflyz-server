package io.brieflyz.subscription_service.adapter.out.persistence.entity

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "subscription", indexes = [Index(name = "idx_email", columnList = "email", unique = true)])
class SubscriptionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val email: String,
    val country: String,
    val city: String,
    @Enumerated(EnumType.STRING)
    val plan: SubscriptionPlan,
    val payCount: Int,
    val deleted: Boolean
) : BaseEntity()