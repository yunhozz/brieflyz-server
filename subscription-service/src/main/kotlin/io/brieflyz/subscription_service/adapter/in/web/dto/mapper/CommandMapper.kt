package io.brieflyz.subscription_service.adapter.`in`.web.dto.mapper

import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateBankTransferDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateCreditCardDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateDigitalWalletDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreatePaymentDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreatePaymentRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateSubscriptionRequest
import io.brieflyz.subscription_service.application.dto.command.CreateBankTransferDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreateCreditCardDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreateDigitalWalletDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreatePaymentCommand
import io.brieflyz.subscription_service.application.dto.command.CreatePaymentDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreateSubscriptionCommand

fun CreateSubscriptionRequest.toCommand(username: String) = CreateSubscriptionCommand(
    email = username,
    country,
    city,
    plan,
    paymentCommand = payment.toCommand()
)

private fun CreatePaymentRequest.toCommand(): CreatePaymentCommand =
    CreatePaymentCommand(charge, method, paymentDetailsCommand = details.toCommand())

private fun CreatePaymentDetailsRequest.toCommand(): CreatePaymentDetailsCommand = when (this) {
    is CreateCreditCardDetailsRequest -> CreateCreditCardDetailsCommand(
        cardNumber = this.cardNumber!!,
        expirationDate = this.expirationDate!!,
        cvc = this.cvc!!
    )

    is CreateBankTransferDetailsRequest -> CreateBankTransferDetailsCommand(
        bankName = this.bankName!!,
        accountNumber = this.accountNumber!!,
        accountHolderName = this.accountHolderName!!,
        routingNumber = this.routingNumber!!
    )

    is CreateDigitalWalletDetailsRequest -> CreateDigitalWalletDetailsCommand(
        walletType = this.walletType!!,
        walletAccountId = this.walletAccountId!!
    )
}