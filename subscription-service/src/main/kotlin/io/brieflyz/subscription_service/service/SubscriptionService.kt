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
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

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
        val (email, country, city, plan, paymentRequest) = request
        log.debug("User Request : {}", request)

        val foundSubscription = subscriptionRepository.findByMemberIdAndPlan(memberId, SubscriptionPlan.of(plan))
        val subscription = foundSubscription
            ?.takeUnless { it.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED) }
            ?: run {
                foundSubscription?.let { throw AlreadyUnlimitedPlanException() }
                Subscription(
                    memberId,
                    email,
                    country,
                    city,
                    plan = SubscriptionPlan.of(plan)
                )
            }

        log.debug("Subscription Information : {}", subscription.toResponse())

        val (charge, method, paymentDetailsRequest) = paymentRequest
        val paymentMethod = PaymentMethod.of(method)

        val paymentDetails = PaymentDetailsFactory.createByRequest(paymentDetailsRequest)
        val payment = Payment(subscription, charge, paymentMethod, paymentDetails)

        log.info("Payment Method : ${paymentDetails::class.simpleName}")
        log.debug("Payment Information : {}", payment.toResponse())

        paymentRepository.save(payment)
        paymentDetailsRepository.save(paymentDetails)

        log.info("Add Subscription's Pay Count")
        subscription.addPayCount()

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
    @Scheduled(cron = "0 0 0 * * *")
    fun deleteExpiredSubscriptionsEveryDay() {
        val currentTime = LocalDateTime.now()
        val expiredSubscriptions = subscriptionRepository.findLimitedSubscriptions()
            .filter { it.isExpired(currentTime) }

        expiredSubscriptions.forEach { subscription ->
            subscription.delete()
            log.debug(
                "Deleted Subscription : ID={}, Member ID={}, Plan={}",
                subscription.id,
                subscription.memberId,
                subscription.plan
            )
        }

        log.info("A total of ${expiredSubscriptions.size} subscriptions have been successfully deleted.")
    }

    @Transactional
    fun hardDeleteSubscription(id: Long) {
        val subscription = findSubscriptionById(id)
        subscriptionRepository.delete(subscription)
    }

    private fun findSubscriptionById(id: Long): Subscription = subscriptionRepository.findByIdOrNull(id)
        ?: throw SubscriptionNotFoundException("Subscription ID: $id")

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
        charge = this.charge,
        method = this.method.name
    )
}