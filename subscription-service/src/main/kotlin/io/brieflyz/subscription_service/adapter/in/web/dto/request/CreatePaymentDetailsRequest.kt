package io.brieflyz.subscription_service.adapter.`in`.web.dto.request

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CreateCreditCardDetailsRequest::class, name = "CREDIT_CARD"),
    JsonSubTypes.Type(value = CreateBankTransferDetailsRequest::class, name = "BANK_TRANSFER"),
    JsonSubTypes.Type(value = CreateDigitalWalletDetailsRequest::class, name = "DIGITAL_WALLET")
)
sealed interface CreatePaymentDetailsRequest

data class CreateCreditCardDetailsRequest(
    val cardNumber: String?,
    val expirationDate: String?,
    val cvc: String?
) : CreatePaymentDetailsRequest

data class CreateBankTransferDetailsRequest(
    val bankName: String?,
    val accountNumber: String?,
    val accountHolderName: String?,
    val routingNumber: String?
) : CreatePaymentDetailsRequest

data class CreateDigitalWalletDetailsRequest(
    val walletType: String?,
    val walletAccountId: String?
) : CreatePaymentDetailsRequest