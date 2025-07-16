package io.brieflyz.subscription_service.service

import io.brieflyz.subscription_service.common.constants.SubscriptionInterval
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.infra.db.SubscriptionRepository
import io.brieflyz.subscription_service.model.dto.CreateSubscriptionRequest
import io.brieflyz.subscription_service.model.dto.UpdateSubscriptionRequest
import io.brieflyz.subscription_service.model.entity.Subscription
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
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class SubscriptionServiceTest {

    @InjectMocks
    private lateinit var subscriptionService: SubscriptionService

    @Mock
    private lateinit var subscriptionRepository: SubscriptionRepository

    @Test
    fun `createSubscription - 구독 생성 성공`() {
        // given
        val memberId = 1L
        val email = "test@example.com"
        val subscriptionInterval = "one year"

        val request = CreateSubscriptionRequest(memberId, email, subscriptionInterval)
        val subscription = mock(Subscription::class.java)

        given(subscriptionRepository.save(any(Subscription::class.java)))
            .willReturn(subscription)

        // when
        val result = subscriptionService.createSubscription(request)

        // then
        then(subscriptionRepository).should().save(any(Subscription::class.java))
    }

    @Test
    fun `getSubscription - 구독 조회 성공`() {
        // given
        val subscriptionId = 100L
        val subscription = createMockSubscription(subscriptionId)

        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(subscription))

        // when
        val result = subscriptionService.getSubscription(subscriptionId)

        // then
        assertEquals(subscriptionId, result.id)
        assertEquals(1L, result.memberId)
        assertEquals("test@example.com", result.email)
        assertEquals(SubscriptionInterval.ONE_YEAR.name, result.subscriptionInterval)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `getSubscription - 구독이 존재하지 않을 때 예외 발생`() {
        // given
        val subscriptionId = 999L
        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.empty())

        // when & then
        val exception = assertFailsWith<SubscriptionNotFoundException> {
            subscriptionService.getSubscription(subscriptionId)
        }
        assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `getSubscriptionsByMemberIdOrEmail - memberId로 구독 목록 조회`() {
        // given
        val memberId = 1L
        val subscriptions = listOf(
            createMockSubscription(1L),
            createMockSubscription(2L)
        )

        given(subscriptionRepository.findByMemberIdOrEmail(memberId, null))
            .willReturn(subscriptions)

        // when
        val result = subscriptionService.getSubscriptionsByMemberIdOrEmail(memberId, null)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(2L, result[1].id)
        then(subscriptionRepository).should().findByMemberIdOrEmail(memberId, null)
    }

    @Test
    fun `getSubscriptionsByMemberIdOrEmail - email로 구독 목록 조회`() {
        // given
        val email = "test@example.com"
        val subscriptions = listOf(createMockSubscription(1L))

        given(subscriptionRepository.findByMemberIdOrEmail(null, email))
            .willReturn(subscriptions)

        // when
        val result = subscriptionService.getSubscriptionsByMemberIdOrEmail(null, email)

        // then
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        then(subscriptionRepository).should().findByMemberIdOrEmail(null, email)
    }

    @Test
    fun `updateSubscription - 구독 간격 업데이트 성공`() {
        // given
        val subscriptionId = 100L
        val request = UpdateSubscriptionRequest(subscriptionInterval = "one month")
        val subscription = spy(
            Subscription(
                memberId = 1L,
                email = "test@example.com",
                subscriptionInterval = SubscriptionInterval.ONE_YEAR
            )
        )

        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(subscription))

        // when
        val result = subscriptionService.updateSubscription(subscriptionId, request)

        // then
        assertEquals(SubscriptionInterval.ONE_MONTH, subscription.subscriptionInterval)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `updateSubscription - 구독이 존재하지 않을 때 예외 발생`() {
        // given
        val subscriptionId = 999L
        val request = UpdateSubscriptionRequest(subscriptionInterval = "MONTHLY")

        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.empty())

        // when & then
        val exception = assertFailsWith<SubscriptionNotFoundException> {
            subscriptionService.updateSubscription(subscriptionId, request)
        }
        assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `deleteSubscription - 소프트 삭제 성공`() {
        // given
        val subscriptionId = 1L
        val subscription = spy(Subscription::class.java)

        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(subscription))

        // when
        subscriptionService.deleteSubscription(subscriptionId)

        // then
        then(subscriptionRepository).should().findById(subscriptionId)
        then(subscription).should().delete()
    }

    @Test
    fun `deleteSubscription - 구독이 존재하지 않을 때 예외 발생`() {
        // given
        val subscriptionId = 999L
        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.empty())

        // when & then
        val exception = assertFailsWith<SubscriptionNotFoundException> {
            subscriptionService.deleteSubscription(subscriptionId)
        }
        assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `hardDeleteSubscription - 하드 삭제 성공`() {
        // given
        val subscriptionId = 1L
        val subscription = spy(Subscription::class.java)

        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.of(subscription))

        // when
        subscriptionService.hardDeleteSubscription(subscriptionId)

        // then
        then(subscriptionRepository).should().findById(subscriptionId)
        then(subscriptionRepository).should().delete(subscription)
    }

    @Test
    fun `hardDeleteSubscription - 구독이 존재하지 않을 때 예외 발생`() {
        // given
        val subscriptionId = 999L
        given(subscriptionRepository.findById(subscriptionId))
            .willReturn(Optional.empty())

        // when & then
        val exception = assertFailsWith<SubscriptionNotFoundException> {
            subscriptionService.hardDeleteSubscription(subscriptionId)
        }
        assertEquals("해당 구독 정보를 찾을 수 없습니다. Subscription ID: $subscriptionId", exception.message)
        then(subscriptionRepository).should().findById(subscriptionId)
    }

    @Test
    fun `existsByMemberId - 회원 ID로 구독 존재 확인 - true`() {
        // given
        val memberId = 1L
        given(subscriptionRepository.existsByMemberId(memberId))
            .willReturn(true)

        // when
        val result = subscriptionService.existsByMemberId(memberId)

        // then
        assertTrue(result)
        then(subscriptionRepository).should().existsByMemberId(memberId)
    }

    @Test
    fun `existsByMemberId - 회원 ID로 구독 존재 확인 - false`() {
        // given
        val memberId = 999L
        given(subscriptionRepository.existsByMemberId(memberId))
            .willReturn(false)

        // when
        val result = subscriptionService.existsByMemberId(memberId)

        // then
        assertEquals(false, result)
        then(subscriptionRepository).should().existsByMemberId(memberId)
    }

    private fun createMockSubscription(id: Long): Subscription = mock(Subscription::class.java).apply {
        given(this.id).willReturn(id)
        given(this.memberId).willReturn(1L)
        given(this.email).willReturn("test@example.com")
        given(this.subscriptionInterval).willReturn(SubscriptionInterval.ONE_YEAR)
        given(this.deleted).willReturn(false)
        given(this.createdAt).willReturn(LocalDateTime.now())
        given(this.updatedAt).willReturn(LocalDateTime.now())
    }
}