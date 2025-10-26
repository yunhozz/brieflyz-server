package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.dto.command.CreateCreditCardDetailsCommand
import io.brieflyz.subscription_service.application.dto.command.CreatePaymentCommand
import io.brieflyz.subscription_service.application.dto.command.CreateSubscriptionCommand
import io.brieflyz.subscription_service.application.port.out.EmailPort
import io.brieflyz.subscription_service.application.port.out.MessagePort
import io.brieflyz.subscription_service.application.port.out.PaymentDetailsRepositoryPort
import io.brieflyz.subscription_service.application.port.out.PaymentRepositoryPort
import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyHaveSubscriptionException
import io.brieflyz.subscription_service.common.props.SubscriptionServiceProperties
import io.brieflyz.subscription_service.domain.model.Subscription
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

class CreateSubscriptionServiceTest {

    private lateinit var subscriptionRepository: SubscriptionRepositoryPort
    private lateinit var paymentRepository: PaymentRepositoryPort
    private lateinit var paymentDetailsRepository: PaymentDetailsRepositoryPort
    private lateinit var emailPort: EmailPort
    private lateinit var messagePort: MessagePort
    private lateinit var service: CreateSubscriptionService

    @BeforeEach
    fun setUp() {
        subscriptionRepository = mock()
        paymentRepository = mock()
        paymentDetailsRepository = mock()
        emailPort = mock()
        messagePort = mock()

        val emailProps = SubscriptionServiceProperties.EmailProperties().apply {
            dashboardUrl = "http://dashboard.test"
            renewUrl = "http://renew.test"
        }
        val props = SubscriptionServiceProperties().apply {
            email = emailProps
        }

        service = CreateSubscriptionService(
            subscriptionRepository,
            paymentRepository,
            paymentDetailsRepository,
            emailPort,
            messagePort,
            props
        )
    }

    private fun createTestCreditCardDetailsCommand() = CreateCreditCardDetailsCommand(
        cardNumber = "1111-2222-3333-4444",
        expirationDate = "01/30",
        cvc = "123"
    )

    private fun createTestPaymentCommand(): CreatePaymentCommand {
        return CreatePaymentCommand(
            charge = 10000.0,
            method = "CREDIT_CARD",
            paymentDetailsCommand = createTestCreditCardDetailsCommand()
        )
    }

    @Test
    fun `create new subscription successfully`() {
        val paymentCommand = createTestPaymentCommand()
        val command = CreateSubscriptionCommand(
            email = "test@example.com",
            country = "KR",
            city = "Seoul",
            plan = SubscriptionPlan.ONE_YEAR.name,
            paymentCommand = paymentCommand
        )

        // subscription이 없을 때
        whenever(subscriptionRepository.findByEmail("test@example.com")).thenReturn(null)
        whenever(subscriptionRepository.save(any())).thenAnswer { it.arguments[0] as Subscription }
        whenever(paymentDetailsRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(paymentRepository.save(any())).thenAnswer { it.arguments[0] }

        val subscriptionId = service.create(command)

        verify(subscriptionRepository).save(any())
        verify(paymentRepository).save(any())
        verify(emailPort).send(any(), any(), any(), any())
        verify(messagePort).sendSubscriptionMessage(any())
    }

    @Test
    fun `create subscription throws AlreadyHaveSubscriptionException if active subscription exists`() {
        val existingSubscription = mock<Subscription> {
            on { isActivated() } doReturn true
            on { isSubscriptionPlanEquals(SubscriptionPlan.ONE_YEAR) } doReturn false
        }

        whenever(subscriptionRepository.findByEmail("test@example.com")).thenReturn(existingSubscription)

        val paymentCommand = createTestPaymentCommand()
        val command = CreateSubscriptionCommand(
            email = "test@example.com",
            country = "KR",
            city = "Seoul",
            plan = SubscriptionPlan.ONE_YEAR.name,
            paymentCommand = paymentCommand
        )

        assertFailsWith<AlreadyHaveSubscriptionException> {
            service.create(command)
        }
    }
}