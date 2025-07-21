package io.brieflyz.subscription_service.model.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CreditCardDetailsRequest::class, name = "CREDIT_CARD"),
    JsonSubTypes.Type(value = BankTransferDetailsRequest::class, name = "BANK_TRANSFER"),
    JsonSubTypes.Type(value = DigitalWalletDetailsRequest::class, name = "DIGITAL_WALLET")
)
interface PaymentDetailsCreateRequest

data class CreditCardDetailsRequest(
    val cardNumber: String = "",
    val expirationDate: String = "",
    val cvc: String = ""
) : PaymentDetailsCreateRequest

data class BankTransferDetailsRequest(
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val routingNumber: String = ""
) : PaymentDetailsCreateRequest

data class DigitalWalletDetailsRequest(
    val walletType: String = "",
    val walletAccountId: String = ""
) : PaymentDetailsCreateRequest