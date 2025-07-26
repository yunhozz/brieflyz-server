package io.brieflyz.subscription_service.service

import io.brieflyz.subscription_service.model.dto.request.BankTransferDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.CreditCardDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.DigitalWalletDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.PaymentDetailsCreateRequest
import io.brieflyz.subscription_service.model.entity.BankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.CreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.DigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.PaymentDetails
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object PaymentDetailsFactory {

    fun createByRequest(request: PaymentDetailsCreateRequest): PaymentDetails = when (request) {
        is CreditCardDetailsRequest -> CreditCardPaymentDetails(
            request.cardNumber!!,
            parseExpirationDate(request.expirationDate!!),
            request.cvc!!.toInt()
        )

        is BankTransferDetailsRequest -> BankTransferPaymentDetails(
            request.bankName!!,
            request.accountNumber!!,
            request.accountHolderName!!,
            request.routingNumber!!
        )

        is DigitalWalletDetailsRequest -> DigitalWalletPaymentDetails(
            request.walletType!!,
            request.walletAccountId!!
        )
    }

    private fun parseExpirationDate(expirationDate: String): ZonedDateTime =
        try {
            YearMonth.parse(expirationDate, DateTimeFormatter.ofPattern("MM/yy"))
                .atEndOfMonth()
                .atStartOfDay(ZoneId.of("Asia/Seoul"))
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid expiration date format: $expirationDate", e)
        }
}