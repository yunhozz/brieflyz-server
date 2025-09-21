package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class FindAllMembersServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var service: FindAllMembersService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        service = FindAllMembersService(memberRepository)
    }

    @Test
    fun `find all members successfully`() {
        val member1 = mock<Member> {
            on { id } doReturn 1L
            on { email } doReturn "a@test.com"
            on { loginType } doReturn LoginType.LOCAL
        }
        val member2 = mock<Member> {
            on { id } doReturn 2L
            on { email } doReturn "b@test.com"
            on { loginType } doReturn LoginType.SOCIAL
        }

        whenever(memberRepository.findAllMembers()).thenReturn(listOf(member1, member2))

        val results = service.findAllMembers()
        assertEquals(2, results.size)
        assertEquals("a@test.com", results[0].email)
        assertEquals("b@test.com", results[1].email)
    }
}