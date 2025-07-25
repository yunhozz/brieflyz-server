package io.brieflyz.subscription_service.model.dto.response

data class PaymentResponse(
    val id: Long,
    val charge: Double,
    val method: String
)