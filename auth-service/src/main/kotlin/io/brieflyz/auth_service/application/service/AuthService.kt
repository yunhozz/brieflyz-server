package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.command.SignInCommand
import io.brieflyz.auth_service.application.dto.command.SignUpCommand
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.`in`.JoinUseCase
import io.brieflyz.auth_service.application.port.`in`.LoginUseCase
import io.brieflyz.auth_service.application.port.`in`.VerifyEmailUseCase
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.EmailPort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.common.exception.VerifyTokenNotFoundException
import io.brieflyz.auth_service.common.props.AuthServiceProperties
import io.brieflyz.auth_service.domain.model.Member
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Year
import java.util.Base64

@Service
class JoinService(
    private val memberRepositoryPort: MemberRepositoryPort,
    private val cachePort: CachePort,
    private val emailPort: EmailPort,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val props: AuthServiceProperties
) : JoinUseCase {

    @Transactional
    override fun join(command: SignUpCommand): Long {
        val (email, password, nickname) = command

        if (memberRepositoryPort.existsByEmail(email))
            throw UserAlreadyExistsException("Email=$email")

        val member = Member.forLocal(email, passwordEncoder.encode(password), nickname)
        val savedMember = memberRepositoryPort.save(member)

        val verifyUrl = props.email.verifyUrl
        val token = generateVerificationToken()
        val ttl = 24 * 3600 * 1000L // 24 hours

        cachePort.save("VERIFY:$token", email, ttl)

        val contextMap = mapOf(
            "email" to email,
            "nickname" to nickname,
            "verifyUrl" to "$verifyUrl?token=$token",
            "unsubscribeUrl" to "",
            "year" to Year.now().toString()
        )
        emailPort.send(email, contextMap)

        return savedMember.id
    }

    private fun generateVerificationToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

@Service
class LoginService(
    private val memberRepositoryPort: MemberRepositoryPort,
    private val tokenProviderPort: TokenProviderPort,
    private val cachePort: CachePort,
    private val passwordEncoder: BCryptPasswordEncoder
) : LoginUseCase {

    @Transactional(readOnly = true)
    override fun login(command: SignInCommand): TokenResult {
        val (email, password) = command
        val member = memberRepositoryPort.findMemberByEmail(email)
            ?: throw UserNotFoundException("Email=$email")

        validatePassword(member, password)

        val username = member.email
        val tokenResult = tokenProviderPort.generateToken(username, member.roles)

        cachePort.save(username, tokenResult.refreshToken, tokenResult.refreshTokenValidTime)

        return tokenResult
    }

    private fun validatePassword(member: Member, password: String) {
        if (member.loginType == LoginType.SOCIAL)
            throw UserRegisteredBySocialException()
        if (!passwordEncoder.matches(password, member.password))
            throw PasswordNotMatchException()
    }
}

@Service
class VerifyEmailService(
    private val memberRepositoryPort: MemberRepositoryPort,
    private val cachePort: CachePort
) : VerifyEmailUseCase {

    @Transactional
    override fun verifyByToken(token: String) {
        val verifyKey = "VERIFY:$token"
        val email = cachePort.find(verifyKey)
            ?: throw VerifyTokenNotFoundException()

        val member = memberRepositoryPort.findMemberByEmail(email)
            ?: throw UserNotFoundException("Email=$email")

        member.updateByEmailVerify()
        memberRepositoryPort.save(member)

        if (cachePort.exists(email)) cachePort.delete(email)
        cachePort.delete(verifyKey)
    }
}