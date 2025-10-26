package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

class UpdateSubscriptionStatusServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var service: UpdateSubscriptionStatusService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        service = UpdateSubscriptionStatusService(memberRepository)
    }

    @Test
    fun `update subscription status successfully`() {
        val email = "user@test.com"
        val member = mock<Member>()

        whenever(memberRepository.findMemberByEmail(email)).thenReturn(member)
        whenever(memberRepository.save(member)).thenReturn(member)

        service.updateBySubscriptionStatus(email, isCreated = true)

        verify(memberRepository).save(member)
        verify(memberRepository).findMemberByEmail(email)
        verify(memberRepository, never()).delete(any())
    }

    @Test
    fun `update subscription status throws UserNotFoundException`() {
        val email = "user@test.com"
        whenever(memberRepository.findMemberByEmail(email)).thenReturn(null)
        assertFailsWith<UserNotFoundException> {
            service.updateBySubscriptionStatus(email, isCreated = true)
        }
    }
}