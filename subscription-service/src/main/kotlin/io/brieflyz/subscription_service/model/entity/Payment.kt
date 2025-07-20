package io.brieflyz.subscription_service.model.entity

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

@Entity
class Payment(
    @ManyToOne(fetch = FetchType.LAZY)
    val subscription: Subscription,
    val charge: Double,
    @Enumerated(EnumType.STRING)
    val method: PaymentMethod,
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    val details: PaymentDetails?
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
}