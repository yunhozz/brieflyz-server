package io.brieflyz.subscription_service.application.service

import io.brieflyz.subscription_service.application.dto.query.SubscriptionQuery
import io.brieflyz.subscription_service.application.dto.result.PageResult
import io.brieflyz.subscription_service.application.dto.result.PaymentDetailsQueryResult
import io.brieflyz.subscription_service.application.dto.result.PaymentQueryResult
import io.brieflyz.subscription_service.application.dto.result.SubscriptionQueryResult
import io.brieflyz.subscription_service.application.port.out.SubscriptionQueryPort
import io.brieflyz.subscription_service.common.constants.PaymentMethod
import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals

class QuerySubscriptionPageServiceTest {

    private val queryPort: SubscriptionQueryPort = mock()
    private val service = QuerySubscriptionPageService(queryPort)

    @Test
    fun `query subscription page with payments returns expected PageResult`() {
        // given
        val query = SubscriptionQuery(
            page = 1,
            size = 2,
            isDeleted = false,
            email = "test@example.com",
            plan = SubscriptionPlan.ONE_YEAR.name,
            paymentMethod = PaymentMethod.CREDIT_CARD.name
        )

        val paymentDetails1 = PaymentDetailsQueryResult(
            id = 101,
            cardNumber = "1111-2222-3333-4444",
            expirationDate = LocalDateTime.of(2030, 1, 1, 0, 0),
            cvc = 123,
            bankName = null,
            accountNumber = null,
            accountHolderName = null,
            routingNumber = null,
            walletType = null,
            walletAccountId = null
        )

        val payment1 = PaymentQueryResult(
            id = 1,
            subscriptionId = 1,
            paymentDetailsId = 101,
            charge = 5000.0,
            method = PaymentMethod.CREDIT_CARD
        ).apply { details = paymentDetails1 }

        val subscriptionResults = listOf(
            SubscriptionQueryResult(
                id = 1,
                email = "test1@example.com",
                plan = SubscriptionPlan.ONE_YEAR,
                payCount = 1,
                updatedAt = LocalDateTime.now()
            ).apply { payments = listOf(payment1) },

            SubscriptionQueryResult(
                id = 2,
                email = "test2@example.com",
                plan = SubscriptionPlan.ONE_YEAR,
                payCount = 0,
                updatedAt = LocalDateTime.now()
            )
        )

        val expectedPage = PageResult(
            content = subscriptionResults,
            page = 1,
            size = 2,
            totalElements = 2,
            totalPages = 1
        )

        whenever(queryPort.queryPageWithSubscriptionQuery(query)).thenReturn(expectedPage)

        // when
        val actualPage = service.queryPageByQuery(query)

        // then
        assertEquals(expectedPage, actualPage)
        assertEquals(2, actualPage.content.size)
        assertEquals("test1@example.com", actualPage.content[0].email)
        assertEquals(1, actualPage.content[0].payments?.size)
        assertEquals("1111-2222-3333-4444", actualPage.content[0].payments?.get(0)?.details?.cardNumber)
    }
}