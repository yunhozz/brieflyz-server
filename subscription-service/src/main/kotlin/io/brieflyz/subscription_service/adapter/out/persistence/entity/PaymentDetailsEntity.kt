package io.brieflyz.subscription_service.adapter.out.persistence.entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "payment_details")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class PaymentDetailsEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long
) : BaseEntity()

@Entity
@DiscriminatorValue("CREDIT_CARD")
class CreditCardPaymentDetailsEntity(
    id: Long,
    val cardNumber: String,
    val expirationDate: LocalDateTime,
    val cvc: Int
) : PaymentDetailsEntity(id)

@Entity
@DiscriminatorValue("BANK")
class BankTransferPaymentDetailsEntity(
    id: Long,
    val bankName: String,
    val accountNumber: String,
    val accountHolderName: String,
    val routingNumber: String
) : PaymentDetailsEntity(id)

@Entity
@DiscriminatorValue("DIGITAL_WALLET")
class DigitalWalletPaymentDetailsEntity(
    id: Long,
    val walletType: String,
    val walletAccountId: String
) : PaymentDetailsEntity(id)