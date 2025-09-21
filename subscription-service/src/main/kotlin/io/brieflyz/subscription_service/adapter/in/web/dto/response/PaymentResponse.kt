package io.brieflyz.subscription_service.adapter.`in`.web.dto.response

data class PaymentResponse(
    val id: Long,
    val charge: Double,
    val method: String,
    val details: String?
)