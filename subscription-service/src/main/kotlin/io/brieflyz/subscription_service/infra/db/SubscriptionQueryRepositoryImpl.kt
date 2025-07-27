package io.brieflyz.subscription_service.infra.db

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.QPaymentDetailsQueryResponse
import io.brieflyz.subscription_service.model.dto.response.QPaymentQueryResponse
import io.brieflyz.subscription_service.model.dto.response.QSubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.QSubscriptionSimpleQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionSimpleQueryResponse
import io.brieflyz.subscription_service.model.entity.QBankTransferPaymentDetails
import io.brieflyz.subscription_service.model.entity.QCreditCardPaymentDetails
import io.brieflyz.subscription_service.model.entity.QDigitalWalletPaymentDetails
import io.brieflyz.subscription_service.model.entity.QPayment
import io.brieflyz.subscription_service.model.entity.QPaymentDetails
import io.brieflyz.subscription_service.model.entity.QSubscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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

    override fun findWithPaymentsByIdQuery(id: Long): SubscriptionQueryResponse? {
        val subscriptionQuery = query
            .select(
                QSubscriptionQueryResponse(
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
                    QPaymentQueryResponse(
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
                .where(subscription.id.eq(sq.id))
                .orderBy(payment.createdAt.desc())
                .fetch()

            val paymentDetailsQueryList = query
                .select(
                    QPaymentDetailsQueryResponse(
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

    override fun findPageWithPaymentsQuery(
        request: SubscriptionQueryRequest,
        pageable: Pageable
    ): Page<SubscriptionSimpleQueryResponse> {
        val (isDeleted, memberId, email, plan, paymentMethod, order) = request
        val subscriptionQueryList = query
            .select(
                QSubscriptionSimpleQueryResponse(
                    subscription.id,
                    subscription.memberId,
                    subscription.plan,
                    subscription.payCount,
                    subscription.updatedAt
                )
            )
            .from(subscription)
            .where(
                isDeletedEq(isDeleted),
                memberIdEq(memberId),
                emailEq(email),
                planEq(plan)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(OrderSpecifier(order, subscription.updatedAt))
            .fetch()

        val subscriptionCount = query
            .select(subscription.count())
            .from(subscription)
            .fetchOne() ?: 0L

        return PageImpl(subscriptionQueryList, pageable, subscriptionCount)
    }

    private fun isDeletedEq(isDeleted: Boolean?) = isDeleted?.let { subscription.deleted.eq(it) }

    private fun memberIdEq(memberId: Long?) = memberId?.let { subscription.memberId.eq(it) }

    private fun emailEq(email: String?) =
        if (email.isNullOrBlank()) null else subscription.email.eq(email)

    private fun planEq(plan: String?) =
        if (plan.isNullOrBlank()) null else subscription.plan.eq(SubscriptionPlan.of(plan))
}