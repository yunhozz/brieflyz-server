package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.port.out.PaymentRepositoryPort
import io.brieflyz.subscription_service.application.port.out.SubscriptionRepositoryPort
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class DeleteSubscriptionServiceTest {

    private val subscriptionRepository: SubscriptionRepositoryPort = mock()
    private val paymentRepository: PaymentRepositoryPort = mock()
    private val service = DeleteSubscriptionService(subscriptionRepository, paymentRepository)

    @Test
    fun `delete subscription and payments`() {
        val subscriptionId = 1L

        service.delete(subscriptionId)

        verify(paymentRepository).deleteAllBySubscriptionId(subscriptionId)
        verify(subscriptionRepository).deleteById(subscriptionId)
    }
}