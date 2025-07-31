package io.brieflyz.subscription_service.model.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import java.time.ZonedDateTime

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class PaymentDetails : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
class CreditCardPaymentDetails(
    val cardNumber: String,
    val expirationDate: ZonedDateTime,
    val cvc: Int
) : PaymentDetails()

@Entity
@DiscriminatorValue("BANK")
class BankTransferPaymentDetails(
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val routingNumber: String
) : PaymentDetails()

@Entity
@DiscriminatorValue("DIGITAL_WALLET")
class DigitalWalletPaymentDetails(
    val walletType: String,
    val walletAccountId: String
) : PaymentDetails()