package io.brieflyz.subscription_service.model.dto

import io.brieflyz.subscription_service.common.annotation.CustomValidate
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.model.dto.validate.Validatable
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

@CustomValidate
data class PaymentCreateRequest(
    @field:NotNull(message = "결제 금액은 필수입니다.")
    val charge: Double,

    @field:NotBlank(message = "결제 방식은 필수입니다.")
    val method: String,

    val details: PaymentDetailsCreateRequest
) : Validatable {

    override fun validate(ctx: ConstraintValidatorContext): Boolean = when (PaymentMethod.of(method)) {
        PaymentMethod.CREDIT_CARD -> {
            val creditCard = details as CreditCardDetailsRequest
            when {
                creditCard.cardNumber.isBlank() -> ctx.addViolation("카드 번호는 필수입니다.", "cardNumber")
                creditCard.expirationDate.isBlank() -> ctx.addViolation("만료일은 필수입니다.", "expirationDate")
                creditCard.cvc.isBlank() -> ctx.addViolation("CVC는 필수입니다.", "cvc")
                else -> true
            }
        }

        PaymentMethod.BANK_TRANSFER -> {
            val bank = details as BankTransferDetailsRequest
            when {
                bank.bankName.isBlank() -> ctx.addViolation("은행명은 필수입니다.", "bankName")
                bank.accountNumber.isBlank() -> ctx.addViolation("계좌 번호는 필수입니다.", "accountNumber")
                bank.accountHolderName.isBlank() -> ctx.addViolation("예금주 이름은 필수입니다.", "accountHolderName")
                bank.routingNumber.isBlank() -> ctx.addViolation("라우팅 번호는 필수입니다.", "routingNumber")
                else -> true
            }
        }

        PaymentMethod.DIGITAL_WALLET -> {
            val digitalWallet = details as DigitalWalletDetailsRequest
            when {
                digitalWallet.walletType.isBlank() -> ctx.addViolation("지갑 유형은 필수입니다.", "walletType")
                digitalWallet.walletAccountId.isBlank() -> ctx.addViolation("지갑 계정 ID는 필수입니다.", "walletAccountId")
                else -> true
            }
        }
    }
}