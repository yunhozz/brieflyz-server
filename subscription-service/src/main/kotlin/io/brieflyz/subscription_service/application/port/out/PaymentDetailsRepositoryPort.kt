package io.brieflyz.subscription_service.application.port.out

import io.brieflyz.subscription_service.domain.model.PaymentDetails

interface PaymentDetailsRepositoryPort {
    fun save(paymentDetails: PaymentDetails): PaymentDetails
}