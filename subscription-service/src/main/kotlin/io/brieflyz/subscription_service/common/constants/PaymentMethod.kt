package io.brieflyz.subscription_service.common.constants

enum class PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET
    ;

    companion object {
        fun of(method: String): PaymentMethod = entries.find { it.name == method }
            ?: throw IllegalArgumentException("Unknown Payment Method : $method")
    }
}