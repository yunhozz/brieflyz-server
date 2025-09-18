package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult
import io.brieflyz.subscription_service.application.port.out.SubscriptionQueryPort
import io.brieflyz.subscription_service.common.exception.SubscriptionNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class QuerySubscriptionDetailsServiceTest {

    private val queryPort: SubscriptionQueryPort = mock()
    private val service = QuerySubscriptionDetailsService(queryPort)

    @Test
    fun `query existing subscription returns result`() {
        val subscriptionId = 1L
        val result = mock<SubscriptionQueryResult>()
        whenever(queryPort.queryWithPaymentsById(subscriptionId)).thenReturn(result)

        val res = service.queryBySubscriptionId(subscriptionId)
        assert(res == result)
    }

    @Test
    fun `query non-existing subscription throws exception`() {
        val subscriptionId = 999L
        whenever(queryPort.queryWithPaymentsById(subscriptionId)).thenReturn(null)

        assertThrows<SubscriptionNotFoundException> {
            service.queryBySubscriptionId(subscriptionId)
        }
    }
}