package io.brieflyz.subscription_service.model.dto.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.querydsl.core.annotations.QueryProjection
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubscriptionQueryResponse @QueryProjection constructor(
    val id: Long,
    val email: String,
    val country: String?,
    val city: String?,
    val plan: SubscriptionPlan,
    val payCount: Int,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
) {
    var payments: List<PaymentQueryResponse>? = null

    @QueryProjection
    constructor(
        id: Long,
        email: String,
        plan: SubscriptionPlan,
        payCount: Int,
        updatedAt: LocalDateTime
    ) : this(id, email, null, null, plan, payCount, null, updatedAt)
}

data class PaymentQueryResponse @QueryProjection constructor(
    val id: Long,
    val subscriptionId: Long,
    val paymentDetailsId: Long,
    val charge: Double,
    val method: PaymentMethod
) {
    var details: PaymentDetailsQueryResponse? = null
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PaymentDetailsQueryResponse @QueryProjection constructor(
    val id: Long,
    val cardNumber: String?,
    val expirationDate: LocalDateTime?,
    val cvc: Int?,
    val bankName: String?,
    val accountNumber: String?,
    val accountHolderName: String?,
    val routingNumber: String?,
    val walletType: String?,
    val walletAccountId: String?
)