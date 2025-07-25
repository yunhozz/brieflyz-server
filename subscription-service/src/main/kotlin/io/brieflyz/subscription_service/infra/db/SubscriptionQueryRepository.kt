package io.brieflyz.subscription_service.infra.db

import io.brieflyz.subscription_service.model.dto.response.SubscriptionQuery
import io.brieflyz.subscription_service.model.entity.QBankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.QCreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.QDigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.QPayment
import io.brieflyz.subscription_service.model.entity.QPaymentDetails
import io.brieflyz.subscription_service.model.entity.QSubscription

interface SubscriptionQueryRepository {
    fun findAllWithPaymentsByIdQuery(id: Long): SubscriptionQuery?

    val subscription: QSubscription
        get() = QSubscription.subscription
    val payment: QPayment
        get() = QPayment.payment
    val paymentDetails: QPaymentDetails
        get() = QPaymentDetails.paymentDetails
    val creditCardDetails: QCreditCardPaymentDetails
        get() = paymentDetails.`as`(QCreditCardPaymentDetails::class.java)
    val bankTransferDetails: QBankTransferPaymentDetails
        get() = paymentDetails.`as`(QBankTransferPaymentDetails::class.java)
    val digitalWalletDetails: QDigitalWalletPaymentDetails
        get() = paymentDetails.`as`(QDigitalWalletPaymentDetails::class.java)
}