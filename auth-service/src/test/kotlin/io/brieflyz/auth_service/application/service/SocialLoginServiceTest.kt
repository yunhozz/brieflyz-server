package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.command.SocialLoginCommand
import io.brieflyz.auth_service.application.dto.result.SocialUserResult
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class SocialLoginServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var service: SocialLoginService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        service = SocialLoginService(memberRepository)
    }

    @Test
    fun `loginOrRegisterSocialUser - existing social member`() {
        val member = Member.forSocial("user@test.com", "google_123")
        val command = SocialLoginCommand.of(
            attributes = mapOf(
                "sub" to "google",
                "email" to member.email,
                "name" to member.nickname,
                "picture" to "pic"
            ),
            userNameAttributeName = "",
            provider = "google"
        )

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(member)

        val result: SocialUserResult = service.loginOrRegisterSocialUser(command)

        assertEquals("user@test.com", result.email)
        verify(memberRepository, never()).save(any())
    }

    @Test
    fun `loginOrRegisterSocialUser - existing local member gets upgraded`() {
        val member = Member.forLocal("user@test.com", "password", "nickname")
        val command = SocialLoginCommand.of(
            attributes = mapOf(
                "sub" to "google",
                "email" to member.email,
                "name" to member.nickname,
                "picture" to "pic"
            ),
            userNameAttributeName = "",
            provider = "google"
        )

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(member)
        whenever(memberRepository.save(member)).thenReturn(member)

        val result = service.loginOrRegisterSocialUser(command)

        assertEquals("user@test.com", result.email)
        verify(memberRepository).save(member)
    }

    @Test
    fun `loginOrRegisterSocialUser - new member is registered`() {
        val member = Member.forSocial("new@test.com", "google_123")
        val command = SocialLoginCommand.of(
            attributes = mapOf(
                "sub" to "google",
                "email" to member.email,
                "name" to member.nickname,
                "picture" to "pic"
            ),
            userNameAttributeName = "",
            provider = "google"
        )

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(null)
        whenever(memberRepository.save(any<Member>())).thenReturn(member)

        val result = service.loginOrRegisterSocialUser(command)

        assertEquals("new@test.com", result.email)
        verify(memberRepository).save(any<Member>())
    }
}