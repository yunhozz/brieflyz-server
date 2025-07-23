package io.brieflyz.subscription_service.service

import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyUnlimitedPlanException
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.infra.db.PaymentDetailsRepository
import io.brieflyz.subscription_service.infra.db.PaymentRepository
import io.brieflyz.subscription_service.infra.db.SubscriptionRepository
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionUpdateRequest
import io.brieflyz.subscription_service.model.dto.response.PaymentResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionResponse
import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.Subscription
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentDetailsRepository: PaymentDetailsRepository
) {
    private val log = logger()

    @Transactional
    fun createSubscription(memberId: Long, request: SubscriptionCreateRequest): Long {
        // TODO: 구독하고자 하는 유저의 ID 조회
        val (email, country, city, plan, paymentRequest) = request
        log.debug("User Request : {}", request)

        val subscription = subscriptionRepository.findByMemberId(memberId)?.let { subscription ->
            if (subscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED)) {
                log.warn("Already Have UNLIMITED Plan!")
                throw AlreadyUnlimitedPlanException() // Request update payment method instead
            }
            subscription
        } ?: Subscription(
            memberId,
            email,
            country,
            city,
            plan = SubscriptionPlan.of(plan)
        )

        log.debug("Subscription Information : {}", subscription.toResponse())

        val (charge, method, paymentDetailsRequest) = paymentRequest
        val paymentMethod = PaymentMethod.of(method)

        val paymentDetails = PaymentDetailsFactory.createByRequest(paymentDetailsRequest)
        val payment = Payment(subscription, charge, paymentMethod, paymentDetails)

        log.info("Payment Method : ${paymentDetails::class.simpleName}")
        log.debug("Payment Information : {}", payment.toResponse())

        paymentRepository.save(payment)
        paymentDetailsRepository.save(paymentDetails)

        log.info("Add Subscription Count")
        subscription.addCount()

        return subscriptionRepository.save(subscription).id
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

    private fun findSubscriptionById(id: Long): Subscription = subscriptionRepository.findByIdOrNull(id)
        ?: throw SubscriptionNotFoundException("Subscription ID: $id")

    private fun Subscription.toResponse() = SubscriptionResponse(
        id = this.id,
        memberId = this.memberId,
        email = this.email,
        country = this.country,
        city = this.city,
        plan = this.plan.name,
        count = this.count,
        deleted = this.deleted,
        createdAt = this.createdAt.toString(),
        updatedAt = this.updatedAt.toString()
    )

    private fun Payment.toResponse() = PaymentResponse(
        charge = this.charge,
        method = this.method.name
    )
}