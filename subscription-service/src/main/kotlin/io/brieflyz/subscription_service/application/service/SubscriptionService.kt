package io.brieflyz.subscription_service.application.service

import io.brieflyz.core.constants.KafkaTopic
import io.brieflyz.core.dto.kafka.SubscriptionMessage
import io.brieflyz.core.utils.logger
import io.brieflyz.subscription_service.application.dto.command.CreateSubscriptionCommand
import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult
import io.brieflyz.subscription_service.application.port.`in`.CancelSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.CreateSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.DeleteSubscriptionUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionDetailsUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionListUseCase
import io.brieflyz.subscription_service.application.port.`in`.QuerySubscriptionPageUseCase
import io.brieflyz.subscription_service.application.port.out.EmailPort
import io.brieflyz.subscription_service.application.port.out.MessagePort
import io.brieflyz.subscription_service.application.port.out.PaymentDetailsRepositoryPort
import io.brieflyz.subscription_service.application.port.out.PaymentRepositoryPort
import io.brieflyz.subscription_service.application.port.out.SubscriptionQueryPort
import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyHaveSubscriptionException
import io.brieflyz.subscription_service.common.exception.AlreadyHaveUnlimitedSubscriptionException
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.common.props.SubscriptionServiceProperties
import io.brieflyz.subscription_service.domain.model.Payment
import io.brieflyz.subscription_service.domain.model.Subscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.Year
import java.time.format.DateTimeFormatter

@Service
class CreateSubscriptionService(
    private val subscriptionRepositoryPort: SubscriptionRepositoryPort,
    private val paymentRepositoryPort: PaymentRepositoryPort,
    private val paymentDetailsRepositoryPort: PaymentDetailsRepositoryPort,
    private val emailPort: EmailPort,
    private val messagePort: MessagePort,
    private val props: SubscriptionServiceProperties
) : CreateSubscriptionUseCase {

    private val log = logger()

    companion object {
        const val EMAIL_SUBJECT = "[Brieflyz] 구독 완료 안내 메일입니다."
        const val TEMPLATE_NAME = "subscription-completed-email"
    }

    @Transactional
    override fun create(command: CreateSubscriptionCommand): Long {
        val email = command.email
        log.debug("Member Email=$email, Subscription Command={}", command)
        val paymentCommand = command.paymentCommand
        val subscription = subscriptionRepositoryPort.findByEmail(email)
            ?.let { subscription ->
                validateSubscriptionExist(subscription)
                subscription.reSubscribe(SubscriptionPlan.of(command.plan))
            } ?: Subscription.create(
            email,
            country = command.country,
            city = command.city,
            plan = SubscriptionPlan.of(command.plan)
        )

        val paymentDetailsCommand = paymentCommand.paymentDetailsCommand
        val paymentDetailsFactory = PaymentDetailsFactoryProvider.getFactory(paymentDetailsCommand)

        val paymentDetails = paymentDetailsFactory.createPaymentDetails()
        val savedPaymentDetails = paymentDetailsRepositoryPort.save(paymentDetails)
        log.debug("Payment Method=${paymentDetails::class.simpleName}")

        subscription.addPayCount()
        val savedSubscription = subscriptionRepositoryPort.save(subscription)

        val payment = Payment.create(
            subscription = savedSubscription,
            charge = paymentCommand.charge,
            method = PaymentMethod.of(paymentCommand.method),
            details = savedPaymentDetails
        )
        paymentRepositoryPort.save(payment)

        val now = LocalDateTime.now()
        val subscriptionPlan = savedSubscription.plan

        log.info("Successfully created subscription for email: $email, plan: $subscriptionPlan")

        val contextMap = mapOf(
            "sentAt" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "email" to email,
            "planName" to subscriptionPlan.displayName,
            "startDate" to now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            "price" to "₩ ${payment.charge.toInt()}",
            "dashboardUrl" to props.email.dashboardUrl,
            "supportUrl" to "",
            "unsubscribeUrl" to "",
            "year" to Year.now().toString()
        )
        emailPort.send(email, EMAIL_SUBJECT, TEMPLATE_NAME, contextMap)

        val message = SubscriptionMessage(email, isCreated = true)
        messagePort.send(KafkaTopic.SUBSCRIPTION_TOPIC, message)

        return savedSubscription.id
    }

    private fun validateSubscriptionExist(subscription: Subscription) {
        if (subscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED))
            throw AlreadyHaveUnlimitedSubscriptionException("Email=${subscription.email}")
        else if (subscription.isActivated())
            throw AlreadyHaveSubscriptionException("Email=${subscription.email}, Plan=${subscription.plan}")
    }
}

@Service
class QuerySubscriptionDetailsService(
    private val subscriptionQueryPort: SubscriptionQueryPort
) : QuerySubscriptionDetailsUseCase {

    @Transactional(readOnly = true)
    override fun queryBySubscriptionId(subscriptionId: Long): SubscriptionQueryResult =
        subscriptionQueryPort.queryWithPaymentsById(subscriptionId)
            ?: throw SubscriptionNotFoundException("Subscription ID=$subscriptionId")
}

@Service
class QuerySubscriptionListService(
    private val subscriptionQueryPort: SubscriptionQueryPort
) : QuerySubscriptionListUseCase {

    @Transactional(readOnly = true)
    override fun queryListByMemberEmail(email: String): List<SubscriptionQueryResult> =
        subscriptionQueryPort.queryListByMemberEmail(email)
}

@Service
class QuerySubscriptionPageService(
    private val subscriptionQueryPort: SubscriptionQueryPort
) : QuerySubscriptionPageUseCase {

    @Transactional(readOnly = true)
    override fun queryPageByQuery(query: SubscriptionQuery): PageResult<SubscriptionQueryResult> =
        subscriptionQueryPort.queryPageWithSubscriptionQuery(query)
}

@Service
class CancelSubscriptionService(
    private val subscriptionRepositoryPort: SubscriptionRepositoryPort
) : CancelSubscriptionUseCase {

    private val log = logger()

    @Transactional
    override fun cancel(subscriptionId: Long): Long {
        val subscription = subscriptionRepositoryPort.findById(subscriptionId)
            ?: throw SubscriptionNotFoundException("Subscription ID=$subscriptionId")

        if (!subscription.isActivated()) {
            throw SubscriptionNotFoundException("Subscription ID=$subscriptionId")
        }

        subscription.delete()
        subscriptionRepositoryPort.save(subscription)
        log.info("Canceled Subscription Details={}", subscription)

        return subscription.id
    }
}

@Service
class DeleteSubscriptionService(
    private val subscriptionRepositoryPort: SubscriptionRepositoryPort,
    private val paymentRepositoryPort: PaymentRepositoryPort
) : DeleteSubscriptionUseCase {

    private val log = logger()

    @Transactional
    override fun delete(subscriptionId: Long) {
        paymentRepositoryPort.deleteAllBySubscriptionId(subscriptionId)
        subscriptionRepositoryPort.deleteById(subscriptionId)

        log.info("Subscription and payments have been successfully deleted.")
    }
}