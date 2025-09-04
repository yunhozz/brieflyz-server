package io.brieflyz.subscription_service.adapter.out.persistence.entity

import io.brieflyz.subscription_service.common.constants.PaymentMethod
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    @ManyToOne(fetch = FetchType.LAZY)
    val subscription: SubscriptionEntity,
    val charge: Double,
    @Enumerated(EnumType.STRING)
    val method: PaymentMethod,
    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.REMOVE])
    val details: PaymentDetailsEntity
) : BaseEntity()