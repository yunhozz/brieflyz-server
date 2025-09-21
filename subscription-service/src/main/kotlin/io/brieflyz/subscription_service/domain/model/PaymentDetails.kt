package io.brieflyz.subscription_service.domain.model

import java.time.LocalDateTime

sealed class PaymentDetails(
    val id: Long
)

class CreditCardPaymentDetails(
    id: Long,
    val cardNumber: String,
    val expirationDate: LocalDateTime,
    val cvc: Int
) : PaymentDetails(id)

class BankTransferPaymentDetails(
    id: Long,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val routingNumber: String
) : PaymentDetails(id)

class DigitalWalletPaymentDetails(
    id: Long,
    val walletType: String,
    val walletAccountId: String
) : PaymentDetails(id)