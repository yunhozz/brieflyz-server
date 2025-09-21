package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.command.SignUpCommand
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.EmailPort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.props.AuthServiceProperties
import io.brieflyz.auth_service.domain.model.Member
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertFailsWith

class JoinServiceTest {

    private lateinit var memberRepository: MemberRepositoryPort
    private lateinit var cachePort: CachePort
    private lateinit var emailPort: EmailPort
    private lateinit var passwordEncoder: BCryptPasswordEncoder
    private lateinit var props: AuthServiceProperties
    private lateinit var service: JoinService

    @BeforeEach
    fun setUp() {
        memberRepository = mock()
        cachePort = mock()
        emailPort = mock()
        passwordEncoder = BCryptPasswordEncoder()
        props = AuthServiceProperties(
            email = AuthServiceProperties.EmailProperties().apply {
                verifyUrl = "http://localhost/verify"
            }
        )
        service = JoinService(memberRepository, cachePort, emailPort, passwordEncoder, props)
    }

    @Test
    fun `join successfully`() {
        val command = SignUpCommand("user@test.com", "password", "nickname")
        val member = Member.forLocal("user@test.com", "encodedPassword", "nickname")

        whenever(memberRepository.existsByEmail(command.email)).thenReturn(false)
        whenever(memberRepository.save(any())).thenReturn(member)

        val resultId = service.join(command)

        verify(memberRepository).save(any())
        verify(cachePort).save(any<String>(), eq(command.email), any<Long>())
        verify(emailPort).send(eq(command.email), any())
    }

    @Test
    fun `join throws UserAlreadyExistsException`() {
        val command = SignUpCommand("user@test.com", "password", "nickname")

        whenever(memberRepository.existsByEmail(command.email)).thenReturn(true)

        assertFailsWith<UserAlreadyExistsException> {
            service.join(command)
        }
    }
}