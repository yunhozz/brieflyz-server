package io.brieflyz.subscription_service.service

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.SubscriptionMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyHaveSubscriptionException
import io.brieflyz.subscription_service.common.exception.AlreadyHaveUnlimitedSubscriptionException
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.config.SubscriptionServiceProperties
import io.brieflyz.subscription_service.model.dto.request.PaymentCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.PaymentResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.dto.response.SubscriptionResponse
import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.PaymentDetails
import io.brieflyz.subscription_service.model.entity.Subscription
import io.brieflyz.subscription_service.repository.PaymentDetailsRepository
import io.brieflyz.subscription_service.repository.PaymentRepository
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import io.brieflyz.subscription_service.service.support.MailProducer
import io.brieflyz.subscription_service.service.support.PaymentDetailsFactoryProvider
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.context.Context
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentRepository: PaymentRepository,
    private val paymentDetailsRepository: PaymentDetailsRepository,
    private val kafkaSender: KafkaSender,
    private val mailProducer: MailProducer,
    private val subscriptionServiceProperties: SubscriptionServiceProperties
) {
    private val log = logger()

    companion object {
        const val EMAIL_SUBJECT = "[Brieflyz] 구독 완료 안내 메일입니다."
        const val TEMPLATE_NAME = "subscription-completed-email"
    }

    @Transactional
    fun createSubscription(email: String, request: SubscriptionCreateRequest): Long {
        log.debug("Member Email: $email, Subscription Request: {}", request)
        val paymentRequest = request.payment
        val subscription = subscriptionRepository.findByEmail(email)
            ?.let { subscription ->
                validateSubscriptionExist(subscription)
                subscription.reSubscribe(SubscriptionPlan.of(request.plan))
            } ?: request.toSubscription(email)

        log.debug("Subscription Information : {}", subscription.toResponse())

        val paymentDetailsCreateRequest = paymentRequest.details
        val paymentDetails = PaymentDetailsFactoryProvider
            .getFactory(paymentDetailsCreateRequest)
            .createPaymentDetails()
        val payment = paymentRequest.toPayment(subscription, paymentDetails)

        log.debug("Payment Method : ${paymentDetails::class.simpleName}")
        log.debug("Payment Information : {}", payment.toResponse())

        paymentRepository.save(payment)
        paymentDetailsRepository.save(paymentDetails)
        subscription.addPayCount()

        val now = LocalDateTime.now()
        val plan = subscription.plan

        log.info("Successfully created subscription for email: $email, plan: $plan")

        val context = Context().apply {
            setVariable("sentAt", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            setVariable("email", email)
            setVariable("planName", plan.displayName)
            setVariable("startDate", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            setVariable("price", "₩${payment.charge.toInt()}")
            setVariable("dashboardUrl", subscriptionServiceProperties.email.dashboardUrl)
            setVariable("supportUrl", "")
            setVariable("unsubscribeUrl", "")
            setVariable("year", Year.now().toString())
        }
        mailProducer.sendAsync(email, EMAIL_SUBJECT, TEMPLATE_NAME, context)

        val message = SubscriptionMessage(email, isCreated = true)
        kafkaSender.send(KafkaTopic.SUBSCRIPTION_TOPIC, message)

        return subscriptionRepository.save(subscription).id
    }

    @Transactional(readOnly = true)
    fun getSubscriptionDetailsById(id: Long): SubscriptionQueryResponse =
        subscriptionRepository.findWithPaymentsByIdQuery(id)
            ?: throw SubscriptionNotFoundException("Subscription ID : $id")

    @Transactional(readOnly = true)
    fun getSubscriptionListByMemberEmail(email: String): List<SubscriptionQueryResponse> =
        subscriptionRepository.findListByMemberEmailQuery(email)

    @Transactional(readOnly = true)
    fun getSubscriptionPageByQuery(
        request: SubscriptionQueryRequest,
        pageable: Pageable
    ): List<SubscriptionQueryResponse> {
        val subscriptionPage = subscriptionRepository.findPageWithPaymentsQuery(request, pageable)
        val pageableInfo = subscriptionPage.pageable

        log.debug(
            "[Subscription Page Info] " +
                    "Total Elements: ${subscriptionPage.totalElements}, " +
                    "Total Pages: ${subscriptionPage.totalPages}, " +
                    "Page Size: ${pageableInfo.pageSize}, " +
                    "Page Number: ${pageableInfo.pageNumber}, " +
                    "Subscription Count: ${subscriptionPage.content.size}"
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
        log.info("Canceled Subscription Details : {}", subscription.toResponse())

        return subscription.id
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

    private fun SubscriptionCreateRequest.toSubscription(email: String) = Subscription(
        email,
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