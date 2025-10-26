package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.dto.command.CreateBankTransferDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreateCreditCardDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreateDigitalWalletDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreatePaymentDetailsCommand
import io.brieflyz.subscription_service.domain.model.BankTransferPaymentDetails
import io.brieflyz.subscription_service.domain.model.CreditCardPaymentDetails
import io.brieflyz.subscription_service.domain.model.DigitalWalletPaymentDetails
import io.brieflyz.subscription_service.domain.model.PaymentDetails
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object PaymentDetailsFactoryProvider {
    fun getFactory(command: CreatePaymentDetailsCommand): PaymentDetailsFactory =
        when (command) {
            is CreateCreditCardDetailsCommand -> CreditCardPaymentDetailsFactory(command)
            is CreateBankTransferDetailsCommand -> BankTransferPaymentDetailsFactory(command)
            is CreateDigitalWalletDetailsCommand -> DigitalWalletPaymentDetailsFactory(command)
        }
}

sealed interface PaymentDetailsFactory {
    fun createPaymentDetails(): PaymentDetails
}

private class CreditCardPaymentDetailsFactory(val command: CreatePaymentDetailsCommand) : PaymentDetailsFactory {
    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/yy")
    }

    override fun createPaymentDetails(): PaymentDetails {
        require(command is CreateCreditCardDetailsCommand)
        val monthYear = YearMonth.parse(command.expirationDate, DATETIME_FORMATTER)
        val expirationDate = monthYear.atDay(1).atStartOfDay()

        return CreditCardPaymentDetails(
            id = 0,
            cardNumber = command.cardNumber,
            expirationDate,
            cvc = command.cvc.toInt()
        )
    }
}

private class BankTransferPaymentDetailsFactory(val command: CreatePaymentDetailsCommand) : PaymentDetailsFactory {
    override fun createPaymentDetails(): PaymentDetails {
        require(command is CreateBankTransferDetailsCommand)
        return BankTransferPaymentDetails(
            id = 0,
            bankName = command.bankName,
            accountNumber = command.accountNumber,
            accountHolderName = command.accountHolderName,
            routingNumber = command.routingNumber
        )
    }
}

private class DigitalWalletPaymentDetailsFactory(val command: CreatePaymentDetailsCommand) : PaymentDetailsFactory {
    override fun createPaymentDetails(): PaymentDetails {
        require(command is CreateDigitalWalletDetailsCommand)
        return DigitalWalletPaymentDetails(
            id = 0,
            walletType = command.walletType,
            walletAccountId = command.walletAccountId
        )
    }
}