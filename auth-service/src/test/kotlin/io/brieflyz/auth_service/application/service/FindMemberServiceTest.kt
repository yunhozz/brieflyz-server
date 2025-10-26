package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FindMemberServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var service: FindMemberService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        service = FindMemberService(memberRepository)
    }

    @Test
    fun `find member successfully`() {
        val member = mock<Member> {
            on { id } doReturn 1L
            on { email } doReturn "test@example.com"
            on { loginType } doReturn LoginType.LOCAL
            on { getRoles() } doReturn listOf("USER")
        }

        whenever(memberRepository.findMemberById(1L)).thenReturn(member)

        val result = service.findMemberById(1L)
        assertEquals(1L, result.id)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `find member throws UserNotFoundException`() {
        whenever(memberRepository.findMemberById(1L)).thenReturn(null)
        assertFailsWith<UserNotFoundException> {
            service.findMemberById(1L)
        }
    }
}