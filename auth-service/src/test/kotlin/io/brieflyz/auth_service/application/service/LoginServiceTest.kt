package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.command.SignInCommand
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var tokenProvider: TokenProviderPort
    private lateinit var cachePort: CachePort
    private lateinit var passwordEncoder: BCryptPasswordEncoder
    private lateinit var service: LoginService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        tokenProvider = mock()
        cachePort = mock()
        passwordEncoder = BCryptPasswordEncoder()
        service = LoginService(memberRepository, tokenProvider, cachePort, passwordEncoder)
    }

    @Test
    fun `login successfully`() {
        val command = SignInCommand("user@test.com", "password")
        val member = Member.forLocal("user@test.com", passwordEncoder.encode("password"), "nickname")

        val tokenResult = TokenResult("type", "access", "refresh", 600L, 3600L)

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(member)
        whenever(tokenProvider.generateToken(eq(command.email), any())).thenReturn(tokenResult)

        val result = service.login(command)

        assertEquals("access", result.accessToken)
        verify(cachePort).save(command.email, tokenResult.refreshToken, tokenResult.refreshTokenValidTime)
    }

    @Test
    fun `login throws UserNotFoundException`() {
        val command = SignInCommand("user@test.com", "password")
        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(null)

        assertFailsWith<UserNotFoundException> {
            service.login(command)
        }
    }

    @Test
    fun `login throws PasswordNotMatchException`() {
        val command = SignInCommand("user@test.com", "wrongpass")
        val member = Member.forLocal("user@test.com", passwordEncoder.encode("correctpass"), "nickname")

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(member)

        assertFailsWith<PasswordNotMatchException> {
            service.login(command)
        }
    }

    @Test
    fun `login throws UserRegisteredBySocialException`() {
        val command = SignInCommand("user@test.com", "password")
        val member = Member.forLocal("user@test.com", passwordEncoder.encode("password"), "nickname")

        member.updateBySocialLogin()

        whenever(memberRepository.findMemberByEmail(command.email)).thenReturn(member)

        assertFailsWith<UserRegisteredBySocialException> {
            service.login(command)
        }
    }
}