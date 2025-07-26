package io.brieflyz.subscription_service.infra.db

import com.querydsl.jpa.impl.JPAQueryFactory
import io.brieflyz.subscription_service.model.dto.response.QPaymentDetailsQuery
import io.brieflyz.subscription_service.model.dto.response.QPaymentQuery
import io.brieflyz.subscription_service.model.dto.response.QSubscriptionQuery
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQuery
import io.brieflyz.subscription_service.model.entity.QBankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.QCreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.QDigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.QPayment
import io.brieflyz.subscription_service.model.entity.QPaymentDetails
import io.brieflyz.subscription_service.model.entity.QSubscription
import org.springframework.stereotype.Repository

@Repository
class SubscriptionQueryRepositoryImpl(
    private val query: JPAQueryFactory
) : SubscriptionQueryRepository {

    private val subscription: QSubscription = QSubscription.subscription
    private val payment: QPayment = QPayment.payment
    private val paymentDetails: QPaymentDetails = QPaymentDetails.paymentDetails
    private val creditCardDetails: QCreditCardPaymentDetails =
        paymentDetails.`as`(QCreditCardPaymentDetails::class.java)
    private val bankTransferDetails: QBankTransferPaymentDetails =
        paymentDetails.`as`(QBankTransferPaymentDetails::class.java)
    private val digitalWalletDetails: QDigitalWalletPaymentDetails =
        paymentDetails.`as`(QDigitalWalletPaymentDetails::class.java)

    override fun findWithPaymentsByIdQuery(id: Long): SubscriptionQuery? {
        val subscriptionQuery = query
            .select(
                QSubscriptionQuery(
                    subscription.id,
                    subscription.memberId,
                    subscription.email,
                    subscription.country,
                    subscription.city,
                    subscription.plan,
                    subscription.payCount,
                    subscription.createdAt,
                    subscription.updatedAt
                )
            )
            .from(subscription)
            .where(subscription.id.eq(id))
            .fetchOne()

        subscriptionQuery?.let { sq ->
            val paymentQueryList = query
                .select(
                    QPaymentQuery(
                        payment.id,
                        subscription.id,
                        paymentDetails.id,
                        payment.charge,
                        payment.method
                    )
                )
                .from(payment)
                .join(payment.subscription, subscription)
                .join(payment.details, paymentDetails)
                .where(payment.subscription.id.eq(sq.id))
                .orderBy(payment.id.desc())
                .fetch()

            val paymentDetailsQueryList = query
                .select(
                    QPaymentDetailsQuery(
                        paymentDetails.id,
                        creditCardDetails.cardNumber,
                        creditCardDetails.expirationDate,
                        creditCardDetails.cvc,
                        bankTransferDetails.bankName,
                        bankTransferDetails.accountNumber,
                        bankTransferDetails.accountHolderName,
                        bankTransferDetails.routingNumber,
                        digitalWalletDetails.walletType,
                        digitalWalletDetails.walletAccountId
                    )
                )
                .from(paymentDetails)
                .where(paymentDetails.id.`in`(paymentQueryList.map { it.paymentDetailsId }))
                .fetch()

            paymentDetailsQueryList.associateBy { it.id }.let { paymentDetailsQueryMap ->
                paymentQueryList.forEach { paymentQuery ->
                    val paymentDetailsQuery = paymentDetailsQueryMap[paymentQuery.paymentDetailsId]
                    paymentQuery.details = paymentDetailsQuery
                }
            }

            sq.payments = paymentQueryList
        }

        return subscriptionQuery
    }
}