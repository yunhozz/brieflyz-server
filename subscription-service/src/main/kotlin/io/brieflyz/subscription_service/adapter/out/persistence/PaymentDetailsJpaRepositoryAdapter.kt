package io.brieflyz.subscription_service.adapter.out.persistence

import io.brieflyz.subscription_service.adapter.out.persistence.entity.BankTransferPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.CreditCardPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.DigitalWalletPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.PaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.repository.PaymentDetailsRepository
import io.brieflyz.subscription_service.application.port.out.PaymentDetailsRepositoryPort
import io.brieflyz.subscription_service.domain.model.BankTransferPaymentDetails
import io.brieflyz.subscription_service.domain.model.CreditCardPaymentDetails
import io.brieflyz.subscription_service.domain.model.DigitalWalletPaymentDetails
import io.brieflyz.subscription_service.domain.model.PaymentDetails
import org.springframework.stereotype.Component

@Component
class PaymentDetailsJpaRepositoryAdapter(
    private val paymentDetailsRepository: PaymentDetailsRepository
) : PaymentDetailsRepositoryPort {

    override fun save(paymentDetails: PaymentDetails): PaymentDetails {
        val paymentDetailsEntity = paymentDetailsRepository.save(paymentDetails.toEntity())
        return paymentDetailsEntity.toDomain()!!
    }
}

internal fun PaymentDetails.toEntity() = when (this) {
    is CreditCardPaymentDetails -> CreditCardPaymentDetailsEntity(id, cardNumber, expirationDate, cvc)
    is BankTransferPaymentDetails -> BankTransferPaymentDetailsEntity(
        id,
        bankName,
        accountNumber,
        accountHolderName,
        routingNumber
    )

    is DigitalWalletPaymentDetails -> DigitalWalletPaymentDetailsEntity(id, walletType, walletAccountId)
}

internal fun PaymentDetailsEntity.toDomain() = when (this) {
    is CreditCardPaymentDetailsEntity -> CreditCardPaymentDetails(id, cardNumber, expirationDate, cvc)
    is BankTransferPaymentDetailsEntity -> BankTransferPaymentDetails(
        id,
        bankName,
        accountNumber,
        accountHolderName,
        routingNumber
    )

    is DigitalWalletPaymentDetailsEntity -> DigitalWalletPaymentDetails(id, walletType, walletAccountId)
    else -> null
}