package io.brieflyz.subscription_service.application.dto.result

data class PaymentResult(
    val id: Long,
    val charge: Double,
    val method: String,
    val details: String?
)