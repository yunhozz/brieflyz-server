package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.exception.VerifyTokenNotFoundException
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFailsWith

class VerifyEmailServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var cachePort: CachePort
    private lateinit var service: VerifyEmailService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        cachePort = mock()
        service = VerifyEmailService(memberRepository, cachePort)
    }

    @Test
    fun `verify email successfully`() {
        val token = "test-token"
        val email = "user@test.com"
        val member = mock<Member>()

        whenever(cachePort.find("VERIFY:$token")).thenReturn(email)
        whenever(memberRepository.findMemberByEmail(email)).thenReturn(member)

        service.verifyByToken(token)

        verify(member).updateByEmailVerify()
        verify(memberRepository).save(member)
        verify(cachePort).delete("VERIFY:$token")
    }

    @Test
    fun `verify email throws VerifyTokenNotFoundException`() {
        val token = "test-token"
        whenever(cachePort.find("VERIFY:$token")).thenReturn(null)

        assertFailsWith<VerifyTokenNotFoundException> {
            service.verifyByToken(token)
        }
    }

    @Test
    fun `verify email throws UserNotFoundException`() {
        val token = "test-token"
        val email = "user@test.com"
        whenever(cachePort.find("VERIFY:$token")).thenReturn(email)
        whenever(memberRepository.findMemberByEmail(email)).thenReturn(null)

        assertFailsWith<UserNotFoundException> {
            service.verifyByToken(token)
        }
    }
}