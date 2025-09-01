package io.brieflyz.subscription_service.service

import io.brieflyz.core.beans.kafka.KafkaSender
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.AlreadyHaveSubscriptionException
import io.brieflyz.subscription_service.common.exception.AlreadyHaveUnlimitedSubscriptionException
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.config.SubscriptionServiceProperties
import io.brieflyz.subscription_service.model.dto.request.CreditCardDetailsRequest
import io.brieflyz.subscription_service.model.dto.request.PaymentCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionCreateRequest
import io.brieflyz.subscription_service.model.dto.request.SubscriptionQueryRequest
import io.brieflyz.subscription_service.model.dto.response.SubscriptionQueryResponse
import io.brieflyz.subscription_service.model.entity.Payment
import io.brieflyz.subscription_service.model.entity.PaymentDetails
import io.brieflyz.subscription_service.model.entity.Subscription
import io.brieflyz.subscription_service.repository.PaymentDetailsRepository
import io.brieflyz.subscription_service.repository.PaymentRepository
import io.brieflyz.subscription_service.repository.SubscriptionRepository
import io.brieflyz.subscription_service.service.support.MailProducer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.mock
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class SubscriptionServiceMockTest {

    @InjectMocks
    private lateinit var subscriptionService: SubscriptionService

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Mock
    private lateinit var paymentRepository: PaymentRepository

    @Mock
    private lateinit var paymentDetailsRepository: PaymentDetailsRepository

    @Mock
    private lateinit var kafkaSender: KafkaSender

    @Mock
    private lateinit var mailProducer: MailProducer

    @Mock
    private lateinit var subscriptionServiceProperties: SubscriptionServiceProperties

    private inline fun <reified T> T.setId(id: Long) {
        val field = this!!::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.setLong(this, id)
    }

    private fun createSubscription(
        id: Long,
        email: String = "test@example.com",
        plan: SubscriptionPlan = SubscriptionPlan.ONE_YEAR
    ): Subscription {
        val subscription = Subscription(email, "Korea", "Seoul", plan)
        subscription.setId(id)
        return subscription
    }

    private fun createPaymentRequest() = PaymentCreateRequest(
        charge = 29.99,
        method = "CREDIT_CARD",
        details = CreditCardDetailsRequest(
            cardNumber = "1234-5678-9012-3456",
            expirationDate = "12/25",
            cvc = "123"
        )
    )

    private fun createSubscriptionRequest(plan: String = "ONE_YEAR") = SubscriptionCreateRequest(
        country = "Korea",
        city = "Seoul",
        plan = plan,
        payment = createPaymentRequest()
    )

    @Nested
    inner class CreateSubscriptionTest {
        @Test
        @DisplayName("새로운 구독 생성 성공")
        fun createNewSubscriptionSuccess() {
            // given
            val subscriptionId = 1L
            val email = "test@example.com"
            val request = createSubscriptionRequest()
            val subscription = createSubscription(subscriptionId, email)
            val paymentDetails = mock(PaymentDetails::class.java)
            val payment = mock(Payment::class.java)

            given(subscriptionRepository.findByEmail(email)).willReturn(null)
            given(subscriptionRepository.save(any(Subscription::class.java))).willReturn(subscription)
            given(paymentRepository.save(any(Payment::class.java))).willReturn(payment)
            given(paymentDetailsRepository.save(any(PaymentDetails::class.java))).willReturn(paymentDetails)

            given(subscriptionServiceProperties.email).willReturn(
                SubscriptionServiceProperties.EmailProperties(
                    dashboardUrl = "http://localhost/subscriptions/dashboard",
                    renewUrl = "http://localhost/subscriptions/renew"
                )
            )

            // when
            val result = subscriptionService.createSubscription(email, request)

            // then
            assertEquals(subscriptionId, result)
            then(subscriptionRepository).should().findByEmail(email)
            then(subscriptionRepository).should().save(any(Subscription::class.java))
            then(paymentRepository).should().save(any(Payment::class.java))
            then(paymentDetailsRepository).should().save(any(PaymentDetails::class.java))
        }

        @Test
        @DisplayName("기존 구독자 재구독 성공")
        fun reSubscribeSuccess() {
            // given
            val subscriptionId = 100L
            val email = "test@example.com"
            val request = createSubscriptionRequest()

            val existingSubscription = spy(createSubscription(subscriptionId, email, SubscriptionPlan.of(request.plan)))
            val paymentDetails = mock(PaymentDetails::class.java)
            val payment = mock(Payment::class.java)

            given(subscriptionRepository.findByEmail(email)).willReturn(existingSubscription)
            given(existingSubscription.isActivated()).willReturn(false)
            given(existingSubscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED)).willReturn(false)

            given(subscriptionRepository.save(any(Subscription::class.java))).willReturn(existingSubscription)
            given(paymentRepository.save(any(Payment::class.java))).willReturn(payment)
            given(paymentDetailsRepository.save(any(PaymentDetails::class.java))).willReturn(paymentDetails)

            given(subscriptionServiceProperties.email).willReturn(
                SubscriptionServiceProperties.EmailProperties(
                    dashboardUrl = "http://localhost/subscriptions/dashboard",
                    renewUrl = "http://localhost/subscriptions/renew"
                )
            )

            // when
            val result = subscriptionService.createSubscription(email, request)

            // then
            assertEquals(subscriptionId, result)
            then(existingSubscription).should().reSubscribe(SubscriptionPlan.ONE_YEAR)
            then(existingSubscription).should().addPayCount()
        }

        @Test
        @DisplayName("무제한 구독자가 있을 때 예외 발생")
        fun createSubscriptionFailWithUnlimitedSubscription() {
            // given
            val email = "test@example.com"
            val request = createSubscriptionRequest()
            val existingSubscription = spy(createSubscription(1L, email, SubscriptionPlan.UNLIMITED))

            given(subscriptionRepository.findByEmail(email)).willReturn(existingSubscription)
            given(existingSubscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED)).willReturn(true)

            // when & then
            assertFailsWith<AlreadyHaveUnlimitedSubscriptionException> {
                subscriptionService.createSubscription(email, request)
            }
        }

        @Test
        @DisplayName("활성화된 구독이 있을 때 예외 발생")
        fun createSubscriptionFailWithActiveSubscription() {
            // given
            val email = "test@example.com"
            val request = createSubscriptionRequest()
            val existingSubscription = spy(createSubscription(1L, email))

            given(subscriptionRepository.findByEmail(email)).willReturn(existingSubscription)
            given(existingSubscription.isSubscriptionPlanEquals(SubscriptionPlan.UNLIMITED)).willReturn(false)
            given(existingSubscription.isActivated()).willReturn(true)

            // when & then
            assertFailsWith<AlreadyHaveSubscriptionException> {
                subscriptionService.createSubscription(email, request)
            }
        }
    }

    @Nested
    inner class GetSubscriptionTest {
        @Test
        @DisplayName("ID로 구독 조회 성공")
        fun getSubscriptionByIdSuccess() {
            // given
            val subscriptionId = 100L
            val queryResponse = mock(SubscriptionQueryResponse::class.java)

            given(subscriptionRepository.findWithPaymentsByIdQuery(subscriptionId)).willReturn(queryResponse)

            // when
            val result = subscriptionService.getSubscriptionDetailsById(subscriptionId)

            // then
            assertEquals(queryResponse, result)
            then(subscriptionRepository).should().findWithPaymentsByIdQuery(subscriptionId)
        }

        @Test
        @DisplayName("구독이 존재하지 않을 때 예외 발생")
        fun getSubscriptionByIdFail() {
            // given
            val subscriptionId = 999L

            given(subscriptionRepository.findWithPaymentsByIdQuery(subscriptionId)).willReturn(null)

            // when & then
            val exception = assertFailsWith<SubscriptionNotFoundException> {
                subscriptionService.getSubscriptionDetailsById(subscriptionId)
            }
            assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID : $subscriptionId", exception.message)
            then(subscriptionRepository).should().findWithPaymentsByIdQuery(subscriptionId)
        }

        @Test
        @DisplayName("이메일로 구독 목록 조회")
        fun getSubscriptionsByEmail() {
            // given
            val email = "test@example.com"
            val subscriptions = listOf(
                mock(SubscriptionQueryResponse::class.java),
                mock(SubscriptionQueryResponse::class.java)
            )

            given(subscriptionRepository.findListByMemberEmailQuery(email)).willReturn(subscriptions)

            // when
            val result = subscriptionService.getSubscriptionListByMemberEmail(email)

            // then
            assertEquals(2, result.size)
            then(subscriptionRepository).should().findListByMemberEmailQuery(email)
        }

        @Test
        @DisplayName("쿼리 조건으로 구독 페이지 조회")
        fun getSubscriptionPageByQuery() {
            // given
            val request = mock(SubscriptionQueryRequest::class.java)
            val pageable = PageRequest.of(0, 10)
            val subscriptions = listOf(mock(SubscriptionQueryResponse::class.java))
            val page = PageImpl(subscriptions, pageable, 1)

            given(subscriptionRepository.findPageWithPaymentsQuery(request, pageable)).willReturn(page)

            // when
            val result = subscriptionService.getSubscriptionPageByQuery(request, pageable)

            // then
            assertEquals(1, result.size)
            then(subscriptionRepository).should().findPageWithPaymentsQuery(request, pageable)
        }
    }

    @Nested
    inner class CancelSubscriptionTest {
        @Test
        @DisplayName("구독 취소 성공")
        fun cancelSubscriptionSuccess() {
            // given
            val subscriptionId = 1L
            val subscription = spy(createSubscription(subscriptionId))

            given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.of(subscription))
            given(subscription.isActivated()).willReturn(true)

            // when
            val result = subscriptionService.cancelSubscriptionById(subscriptionId)

            // then
            assertEquals(subscriptionId, result)
            then(subscription).should().delete()
            then(subscriptionRepository).should().findById(subscriptionId)
        }

        @Test
        @DisplayName("구독이 존재하지 않을 때 예외 발생")
        fun cancelSubscriptionFail() {
            // given
            val subscriptionId = 999L
            given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.empty())

            // when & then
            val exception = assertFailsWith<SubscriptionNotFoundException> {
                subscriptionService.cancelSubscriptionById(subscriptionId)
            }
            assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
            then(subscriptionRepository).should().findById(subscriptionId)
        }

        @Test
        @DisplayName("비활성화된 구독 취소 시 예외 발생")
        fun cancelInactiveSubscriptionFail() {
            // given
            val subscriptionId = 1L
            val subscription = spy(createSubscription(subscriptionId))

            given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.of(subscription))
            given(subscription.isActivated()).willReturn(false)

            // when & then
            val exception = assertFailsWith<SubscriptionNotFoundException> {
                subscriptionService.cancelSubscriptionById(subscriptionId)
            }
            assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
        }
    }

    @Nested
    inner class DeleteSubscriptionTest {
        @Test
        @DisplayName("하드 삭제 성공")
        fun hardDeleteSubscriptionSuccess() {
            // given
            val subscriptionId = 1L
            val subscription = createSubscription(subscriptionId)
            val payments = listOf(mock(Payment::class.java), mock(Payment::class.java))

            given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.of(subscription))
            given(paymentRepository.findAllBySubscription(subscription)).willReturn(payments)

            // when
            subscriptionService.hardDeleteSubscriptionById(subscriptionId)

            // then
            then(subscriptionRepository).should().findById(subscriptionId)
            then(paymentRepository).should().findAllBySubscription(subscription)
            then(subscriptionRepository).should().delete(subscription)
            then(paymentRepository).should().deleteAllInBatch(payments)
        }

        @Test
        @DisplayName("구독이 존재하지 않을 때 예외 발생")
        fun hardDeleteSubscriptionFail() {
            // given
            val subscriptionId = 999L
            given(subscriptionRepository.findById(subscriptionId)).willReturn(Optional.empty())

            // when & then
            val exception = assertFailsWith<SubscriptionNotFoundException> {
                subscriptionService.hardDeleteSubscriptionById(subscriptionId)
            }
            assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
            then(subscriptionRepository).should().findById(subscriptionId)
        }
    }
}