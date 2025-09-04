package io.brieflyz.subscription_service.adapter.out.query

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.jpa.impl.JPAQueryFactory
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QBankTransferPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QCreditCardPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QDigitalWalletPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QPaymentDetailsEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QPaymentEntity
import io.brieflyz.subscription_service.adapter.out.persistence.entity.QSubscriptionEntity
import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.QPaymentDetailsQueryResult
import io.brieflyz.subscription_service.application.dto.result.QPaymentQueryResult
import io.brieflyz.subscription_service.application.dto.result.QSubscriptionQueryResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult
import io.brieflyz.subscription_service.application.port.out.SubscriptionQueryPort
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class SubscriptionQueryDslAdapter(
    private val query: JPAQueryFactory
) : SubscriptionQueryPort {

    private val subscription: QSubscriptionEntity = QSubscriptionEntity.subscriptionEntity
    private val payment: QPaymentEntity = QPaymentEntity.paymentEntity
    private val paymentDetails: QPaymentDetailsEntity = QPaymentDetailsEntity.paymentDetailsEntity
    private val creditCardDetails: QCreditCardPaymentDetailsEntity =
        paymentDetails.`as`(QCreditCardPaymentDetailsEntity::class.java)
    private val bankTransferDetails: QBankTransferPaymentDetailsEntity =
        paymentDetails.`as`(QBankTransferPaymentDetailsEntity::class.java)
    private val digitalWalletDetails: QDigitalWalletPaymentDetailsEntity =
        paymentDetails.`as`(QDigitalWalletPaymentDetailsEntity::class.java)

    override fun queryWithPaymentsById(subscriptionId: Long): SubscriptionQueryResult? {
        val subscriptionQuery = query
            .select(
                QSubscriptionQueryResult(
                    subscription.id,
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
            .where(subscription.id.eq(subscriptionId))
            .fetchOne()

        subscriptionQuery?.let { sq ->
            val paymentQueryList = query
                .select(
                    QPaymentQueryResult(
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
                    QPaymentDetailsQueryResult(
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

    override fun queryListByMemberEmail(email: String): List<SubscriptionQueryResult> =
        query
            .select(
                QSubscriptionQueryResult(
                    subscription.id,
                    subscription.email,
                    subscription.plan,
                    subscription.payCount,
                    subscription.updatedAt
                )
            )
            .from(subscription)
            .where(subscription.email.eq(email))
            .orderBy(subscription.createdAt.desc())
            .fetch()

    override fun queryPageWithSubscriptionQuery(q: SubscriptionQuery): PageResult<SubscriptionQueryResult> {
        val (page, size, isDeleted, email, plan, paymentMethod, order) = q
        val subscriptionQueryList = query
            .select(
                QSubscriptionQueryResult(
                    subscription.id,
                    subscription.email,
                    subscription.plan,
                    subscription.payCount,
                    subscription.updatedAt
                )
            )
            .from(subscription)
            .where(
                isDeletedEq(isDeleted),
                emailEq(email),
                planEq(plan)
            )
            .offset(page.toLong())
            .limit(size.toLong())
            .orderBy(OrderSpecifier(order, subscription.updatedAt))
            .fetch()

        val subscriptionCount = query
            .select(subscription.count())
            .from(subscription)
            .fetchOne() ?: 0L

        return PageResult(
            subscriptionQueryList,
            page,
            size,
            totalElements = subscriptionCount,
            totalPages = ceil(subscriptionCount.toDouble() / size).toInt()
        )
    }

    private fun isDeletedEq(isDeleted: Boolean?) = isDeleted?.let { subscription.deleted.eq(it) }

    private fun emailEq(email: String?) =
        if (email.isNullOrBlank()) null else subscription.email.eq(email)

    private fun planEq(plan: String?) =
        if (plan.isNullOrBlank()) null else subscription.plan.eq(SubscriptionPlan.Companion.of(plan))
}