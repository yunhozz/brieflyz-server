package io.brieflyz.subscription_service.service

import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.infra.db.PaymentDetailsRepository
import io.brieflyz.subscription_service.infra.db.PaymentRepository
import io.brieflyz.subscription_service.infra.db.SubscriptionRepository
import io.brieflyz.subscription_service.model.dto.BankTransferDetailsRequest
import io.brieflyz.subscription_service.model.dto.CreditCardDetailsRequest
import io.brieflyz.subscription_service.model.dto.DigitalWalletDetailsRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.SubscriptionUpdateRequest
import io.brieflyz.subscription_service.model.entity.BankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.CreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.DigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentDetailsRepository: PaymentDetailsRepository
) {
    @Transactional
    fun createSubscription(memberId: Long, request: SubscriptionCreateRequest): Long {
        // TODO: 구독하고자 하는 유저의 ID 조회
        val (email, country, city, plan, paymentRequest) = request
        val subscription = Subscription(
            memberId,
            email,
            country,
            city,
            plan = SubscriptionPlan.of(plan)
        )

        val (charge, method, details) = paymentRequest
        val paymentMethod = PaymentMethod.of(method)

        when (paymentMethod) {
            PaymentMethod.CREDIT_CARD -> {
                val creditCard = details as CreditCardDetailsRequest
                val creditCardPaymentDetails = CreditCardPaymentDetails(
                    creditCard.cardNumber,
                    YearMonth.parse(creditCard.expirationDate, DateTimeFormatter.ofPattern("MM/yy"))
                        .atEndOfMonth()
                        .atStartOfDay(ZoneId.of("Asia/Seoul")),
                    creditCard.cvc.toInt()
                )
                paymentDetailsRepository.save(creditCardPaymentDetails)
                paymentRepository.save(Payment(subscription, charge, paymentMethod, creditCardPaymentDetails))
            }

            PaymentMethod.BANK_TRANSFER -> {
                val bank = details as BankTransferDetailsRequest
                val bankTransferPaymentDetails = BankTransferPaymentDetails(
                    bank.bankName,
                    bank.accountNumber,
                    bank.accountHolderName,
                    bank.routingNumber
                )
                paymentDetailsRepository.save(bankTransferPaymentDetails)
                paymentRepository.save(Payment(subscription, charge, paymentMethod, bankTransferPaymentDetails))
            }

            PaymentMethod.DIGITAL_WALLET -> {
                val digitalWallet = details as DigitalWalletDetailsRequest
                val digitalWalletPaymentDetails =
                    DigitalWalletPaymentDetails(digitalWallet.walletType, digitalWallet.walletAccountId)
                paymentDetailsRepository.save(digitalWalletPaymentDetails)
                paymentRepository.save(Payment(subscription, charge, paymentMethod, digitalWalletPaymentDetails))
            }
        }

        val savedSubscription = subscriptionRepository.save(subscription)

        return savedSubscription.id
    }

    @Transactional(readOnly = true)
    fun getSubscription(id: Long): SubscriptionResponse {
        val subscription = findSubscriptionById(id)
        return subscription.toResponse()
    }

    @Transactional(readOnly = true)
    fun getSubscriptionsByMemberIdOrEmail(memberId: Long?, email: String?): List<SubscriptionResponse> =
        subscriptionRepository.findByMemberIdOrEmail(memberId, email)
            .map { it.toResponse() }

    @Transactional
    fun updateSubscription(id: Long, request: SubscriptionUpdateRequest): Long {
        val subscription = findSubscriptionById(id)
        subscription.updateSubscriptionPlan(SubscriptionPlan.of(request.plan))
        return subscription.id
    }

    @Transactional
    fun deleteSubscription(id: Long) {
        val subscription = findSubscriptionById(id)
        subscription.delete()
    }

    @Transactional
    fun hardDeleteSubscription(id: Long) {
        val subscription = findSubscriptionById(id)
        subscriptionRepository.delete(subscription)
    }

    @Transactional(readOnly = true)
    fun existsByMemberId(memberId: Long): Boolean = subscriptionRepository.existsByMemberId(memberId)

    private fun findSubscriptionById(id: Long) = subscriptionRepository.findByIdOrNull(id)
        ?: throw SubscriptionNotFoundException("Subscription ID: $id")

    private fun Subscription.toResponse() = SubscriptionResponse(
        id = this.id,
        memberId = this.memberId,
        email = this.email,
        country = this.country,
        city = this.city,
        plan = this.plan.name,
        deleted = this.deleted,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )
}