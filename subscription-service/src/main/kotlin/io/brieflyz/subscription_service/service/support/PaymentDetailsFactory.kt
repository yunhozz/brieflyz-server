package io.brieflyz.subscription_service.service.support

import io.brieflyz.subscription_service.model.dto.request.BankTransferDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.CreditCardDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.DigitalWalletDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.PaymentDetailsCreateRequest
import io.brieflyz.subscription_service.model.entity.BankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.CreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.DigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.PaymentDetails
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object PaymentDetailsFactoryProvider {
    fun getFactory(request: PaymentDetailsCreateRequest): PaymentDetailsFactory =
        when (request) {
            is CreditCardDetailsRequest -> CreditCardPaymentDetailsFactory(request)
            is BankTransferDetailsRequest -> BankTransferPaymentDetailsFactory(request)
            is DigitalWalletDetailsRequest -> DigitalWalletPaymentDetailsFactory(request)
        }
}

sealed interface PaymentDetailsFactory {
    fun createPaymentDetails(): PaymentDetails
}

private class CreditCardPaymentDetailsFactory(val request: PaymentDetailsCreateRequest) : PaymentDetailsFactory {
    companion object {
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MM/yy")
    }

    override fun createPaymentDetails(): PaymentDetails {
        require(request is CreditCardDetailsRequest)
        val monthYear = YearMonth.parse(request.expirationDate!!, DATETIME_FORMATTER)
        val expirationDate = monthYear.atDay(1).atStartOfDay()

        return CreditCardPaymentDetails(
            request.cardNumber!!,
            expirationDate,
            request.cvc!!.toInt()
        )
    }
}

private class BankTransferPaymentDetailsFactory(val request: PaymentDetailsCreateRequest) : PaymentDetailsFactory {
    override fun createPaymentDetails(): PaymentDetails {
        require(request is BankTransferDetailsRequest)
        return BankTransferPaymentDetails(
            request.bankName!!,
            request.accountNumber!!,
            request.accountHolderName!!,
            request.routingNumber!!
        )
    }
}

private class DigitalWalletPaymentDetailsFactory(val request: PaymentDetailsCreateRequest) : PaymentDetailsFactory {
    override fun createPaymentDetails(): PaymentDetails {
        require(request is DigitalWalletDetailsRequest)
        return DigitalWalletPaymentDetails(
            request.walletType!!,
            request.walletAccountId!!
        )
    }
}