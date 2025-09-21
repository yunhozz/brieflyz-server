package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult
import io.brieflyz.subscription_service.application.port.out.SubscriptionQueryPort
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class QuerySubscriptionListServiceTest {

    private val queryPort: SubscriptionQueryPort = mock()
    private val service = QuerySubscriptionListService(queryPort)

    @Test
    fun `query subscription list by email`() {
        val email = "test@example.com"
        val results = listOf(mock<SubscriptionQueryResult>())
        whenever(queryPort.queryListByMemberEmail(email)).thenReturn(results)

        val res = service.queryListByMemberEmail(email)
        assert(res == results)
    }
}