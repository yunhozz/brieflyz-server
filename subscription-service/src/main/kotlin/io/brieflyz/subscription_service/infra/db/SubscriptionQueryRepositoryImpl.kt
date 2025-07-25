package io.brieflyz.subscription_service.infra.db

import com.querydsl.jpa.impl.JPAQueryFactory
import io.brieflyz.subscription_service.model.dto.response.QPaymentDetailsQuery
import io.brieflyz.subscription_service.model.dto.response.QPaymentQuery
import io.brieflyz.subscription_service.model.dto.response.QSubscriptionQuery
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQuery
import org.springframework.stereotype.Repository

@Repository
class SubscriptionQueryRepositoryImpl(
    private val query: JPAQueryFactory
) : SubscriptionQueryRepository {

    override fun findAllWithPaymentsByIdQuery(id: Long): SubscriptionQuery? {
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