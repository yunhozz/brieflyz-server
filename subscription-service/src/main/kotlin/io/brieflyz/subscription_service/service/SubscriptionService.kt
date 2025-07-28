package io.brieflyz.subscription_service.service

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyHaveSubscriptionException
import io.brieflyz.subscription_service.common.exception.AlreadyHaveUnlimitedSubscriptionException
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.model.dto.request.PaymentCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.PaymentResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionSimpleQueryResponse
import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.PaymentDetails
import io.brieflyz.subscription_service.model.entity.Subscription
import io.brieflyz.subscription_service.repository.PaymentDetailsRepository
import io.brieflyz.subscription_service.repository.PaymentRepository
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import io.brieflyz.subscription_service.service.component.PaymentDetailsFactory
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@EnableScheduling
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentDetailsRepository: PaymentDetailsRepository
) {
    private val log = logger()

    @Transactional
    fun createSubscription(memberId: Long, request: SubscriptionCreateRequest): Long {
        // TODO: 구독하고자 하는 유저의 ID 조회
        log.debug("Member ID: $memberId, Subscription Request: {}", request)
        val paymentRequest = request.payment
        val subscription = subscriptionRepository.findByMemberId(memberId)
            ?.let { subscription ->
                validateSubscriptionExist(subscription)
                subscription.reSubscribe(SubscriptionPlan.of(request.plan))
            } ?: request.toSubscription(memberId)

        log.debug("Subscription Information : {}", subscription.toResponse())

        val paymentDetails = PaymentDetailsFactory.createByRequest(paymentRequest.details)
        val payment = paymentRequest.toPayment(subscription, paymentDetails)

        log.info("Payment Method : ${paymentDetails::class.simpleName}")
        log.debug("Payment Information : {}", payment.toResponse())

        paymentRepository.save(payment)
        paymentDetailsRepository.save(paymentDetails)

        subscription.addPayCount()

        log.info("Successfully created subscription for email: ${request.email}, plan: ${subscription.plan}")

        return subscriptionRepository.save(subscription).id
    }

    @Transactional(readOnly = true)
    fun getSubscriptionDetailsById(id: Long): SubscriptionQueryResponse =
        subscriptionRepository.findWithPaymentsByIdQuery(id)
            ?: throw SubscriptionNotFoundException("Subscription ID : $id")

    @Transactional(readOnly = true)
    fun getSubscriptionPageByQuery(
        request: SubscriptionQueryRequest,
        pageable: Pageable
    ): List<SubscriptionSimpleQueryResponse> {
        val subscriptionPage = subscriptionRepository.findPageWithPaymentsQuery(request, pageable)
        val pageableInfo = subscriptionPage.pageable

        log.debug(
            """
            [Subscription Page Info]
            Total Elements: ${subscriptionPage.totalElements}
            Total Pages: ${subscriptionPage.totalPages}
            Page Size: ${pageableInfo.pageSize}
            Page Number: ${pageableInfo.pageNumber}
            Subscription Count: ${subscriptionPage.content.size}
        """.trimIndent()
        )

        return subscriptionPage.content
    }

    @Transactional
    fun cancelSubscriptionById(id: Long): Long {
        val subscription = findSubscriptionById(id)
        if (!subscription.isActivated()) {
            throw SubscriptionNotFoundException("Subscription ID: $id")
        }

        subscription.delete()
        log.debug("Canceled Subscription Details : {}", subscription.toResponse())

        return subscription.id
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    fun deleteExpiredSubscriptionsEveryDay() {
        val expiredSubscriptionIds = subscriptionRepository.findLimitedSubscriptionsQuery()
            .filter { it.isExpired() }
            .map { it.id }

        expiredSubscriptionIds.chunked(100).forEach { ids ->
            log.debug("Expired Subscription IDs : {}", ids)
        }

        subscriptionRepository.softDeleteSubscriptionsInIdsQuery(expiredSubscriptionIds)
        log.info("A total of ${expiredSubscriptionIds.size} subscriptions have been successfully deleted.")
    }

    @Transactional
    fun hardDeleteSubscriptionById(id: Long) {
        val subscription = findSubscriptionById(id)
        val payments = paymentRepository.findAllBySubscription(subscription)

        subscriptionRepository.delete(subscription)
        paymentRepository.deleteAllInBatch(payments)

        log.debug("Hard Deleted Subscription Details : {}", subscription.toResponse())
        payments.forEach { log.debug("Deleted Payment Details : {}", it) }

        log.info("Subscription and payments have been successfully deleted.")
    }

    private fun findSubscriptionById(id: Long): Subscription = subscriptionRepository.findByIdOrNull(id)
        ?: throw SubscriptionNotFoundException("Subscription ID: $id")

    private fun validateSubscriptionExist(subscription: Subscription) {
        if (subscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED))
            throw AlreadyHaveUnlimitedSubscriptionException("Email : ${subscription.email}")
        else if (subscription.isActivated())
            throw AlreadyHaveSubscriptionException("Email : ${subscription.email}, Plan : ${subscription.plan}")
    }

    private fun SubscriptionCreateRequest.toSubscription(memberId: Long) = Subscription(
        memberId,
        email = this.email,
        country = this.country,
        city = this.city,
        plan = SubscriptionPlan.of(this.plan)
    )

    private fun PaymentCreateRequest.toPayment(subscription: Subscription, details: PaymentDetails) = Payment(
        subscription,
        charge = this.charge,
        method = PaymentMethod.of(this.method),
        details
    )

    private fun Subscription.toResponse() = SubscriptionResponse(
        id = this.id,
        memberId = this.memberId,
        email = this.email,
        country = this.country,
        city = this.city,
        plan = this.plan.name,
        payCount = this.payCount,
        deleted = this.deleted,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )

    private fun Payment.toResponse() = PaymentResponse(
        id = this.id,
        charge = this.charge,
        method = this.method.name,
        details = this.details::class.simpleName
    )
}