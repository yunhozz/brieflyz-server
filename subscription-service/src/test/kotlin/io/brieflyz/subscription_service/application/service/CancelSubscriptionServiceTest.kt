package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import io.brieflyz.subscription_service.domain.model.Subscription
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CancelSubscriptionServiceTest {

    private lateinit var subscriptionRepository: SubscriptionRepositoryPort
    private lateinit var service: CancelSubscriptionService

    @BeforeEach
    fun setUp() {
        subscriptionRepository = mock()
        service = CancelSubscriptionService(subscriptionRepository)
    }

    @Test
    fun `cancel activated subscription`() {
        val subscription = mock<Subscription> {
            on { id } doReturn 1L
            on { isActivated() } doReturn true
        }

        whenever(subscriptionRepository.findById(1L)).thenReturn(subscription)

        val canceledId = service.cancel(1L)

        verify(subscriptionRepository).save(subscription)
        assert(canceledId == 1L)
    }

    @Test
    fun `cancel non-existing subscription throws exception`() {
        whenever(subscriptionRepository.findById(999L)).thenReturn(null)

        assertThrows<SubscriptionNotFoundException> {
            service.cancel(999L)
        }
    }
}