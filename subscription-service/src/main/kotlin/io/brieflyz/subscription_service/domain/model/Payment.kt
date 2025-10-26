package io.brieflyz.subscription_service.domain.model

import io.brieflyz.subscription_service.common.constants.PaymentMethod

class Payment private constructor(
    val subscription: Subscription,
    val charge: Double,
    val method: PaymentMethod,
    val details: PaymentDetails
) {
    companion object {
        fun create(
            subscription: Subscription,
            charge: Double,
            method: PaymentMethod,
            details: PaymentDetails,
            id: Long? = null
        ): Payment {
            val payment = Payment(subscription, charge, method, details)
            id?.let { payment.id = it }
            return payment
        }
    }

    var id: Long = 0
}