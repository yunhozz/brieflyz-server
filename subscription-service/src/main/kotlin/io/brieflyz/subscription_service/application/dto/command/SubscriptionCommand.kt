package io.brieflyz.subscription_service.application.dto.command

data class CreateSubscriptionCommand(
    val email: String,
    val country: String,
    val city: String,
    val plan: String,
    val paymentCommand: CreatePaymentCommand
)

data class CreatePaymentCommand(
    val charge: Double,
    val method: String,
    val paymentDetailsCommand: CreatePaymentDetailsCommand
)

sealed interface CreatePaymentDetailsCommand

data class CreateCreditCardDetailsCommand(
    val cardNumber: String,
    val expirationDate: String,
    val cvc: String
) : CreatePaymentDetailsCommand

data class CreateBankTransferDetailsCommand(
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val routingNumber: String
) : CreatePaymentDetailsCommand

data class CreateDigitalWalletDetailsCommand(
    val walletType: String,
    val walletAccountId: String
) : CreatePaymentDetailsCommand