package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

class WithdrawServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var cachePort: CachePort
    private lateinit var service: WithdrawService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        cachePort = mock()
        service = WithdrawService(memberRepository, cachePort)
    }

    @Test
    fun `withdraw successfully`() {
        val username = "user@test.com"
        val member = mock<Member>()

        whenever(memberRepository.findMemberByEmail(username)).thenReturn(member)

        service.withdraw(username)

        verify(memberRepository).delete(member)
        verify(cachePort).delete(username)
    }

    @Test
    fun `withdraw throws UserNotFoundException`() {
        val username = "user@test.com"
        whenever(memberRepository.findMemberByEmail(username)).thenReturn(null)
        assertFailsWith<UserNotFoundException> {
            service.withdraw(username)
        }
    }
}