package io.brieflyz.subscription_service.adapter.`in`.web.dto.validation

import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateBankTransferDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateCreditCardDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreateDigitalWalletDetailsRequest
import io.brieflyz.subscription_service.adapter.`in`.web.dto.request.CreatePaymentDetailsRequest
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import jakarta.validation.ConstraintValidatorContext

object CreatePaymentRequestValidator {

    private fun ConstraintValidatorContext.addViolation(message: String, property: String) {
        this.buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation()
    }

    fun validate(method: String, request: CreatePaymentDetailsRequest, ctx: ConstraintValidatorContext): Boolean {
        var isValid = true
        return when (PaymentMethod.of(method)) {
            PaymentMethod.CREDIT_CARD -> {
                val creditCard = request as CreateCreditCardDetailsRequest
                if (creditCard.cardNumber.isNullOrBlank()) {
                    ctx.addViolation("카드 번호는 필수입니다.", "cardNumber")
                    isValid = false
                }
                if (creditCard.expirationDate.isNullOrBlank()) {
                    ctx.addViolation("만료일은 필수입니다.", "expirationDate")
                    isValid = false
                }
                if (creditCard.cvc.isNullOrBlank()) {
                    ctx.addViolation("CVC는 필수입니다.", "cvc")
                    isValid = false
                }
                isValid
            }

            PaymentMethod.BANK_TRANSFER -> {
                val bank = request as CreateBankTransferDetailsRequest
                if (bank.bankName.isNullOrBlank()) {
                    ctx.addViolation("은행명은 필수입니다.", "bankName")
                    isValid = false
                }
                if (bank.accountNumber.isNullOrBlank()) {
                    ctx.addViolation("계좌 번호는 필수입니다.", "accountNumber")
                    isValid = false
                }
                if (bank.accountHolderName.isNullOrBlank()) {
                    ctx.addViolation("예금주 이름은 필수입니다.", "accountHolderName")
                    isValid = false
                }
                if (bank.routingNumber.isNullOrBlank()) {
                    ctx.addViolation("라우팅 번호는 필수입니다.", "routingNumber")
                    isValid = false
                }
                isValid
            }

            PaymentMethod.DIGITAL_WALLET -> {
                val wallet = request as CreateDigitalWalletDetailsRequest
                if (wallet.walletType.isNullOrBlank()) {
                    ctx.addViolation("지갑 유형은 필수입니다.", "walletType")
                    isValid = false
                }
                if (wallet.walletAccountId.isNullOrBlank()) {
                    ctx.addViolation("지갑 계정 ID는 필수입니다.", "walletAccountId")
                    isValid = false
                }
                isValid
            }
        }
    }
}