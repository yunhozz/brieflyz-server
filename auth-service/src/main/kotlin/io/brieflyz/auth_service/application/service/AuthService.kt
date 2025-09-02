package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.SignInRequestDto
import io.brieflyz.auth_service.application.dto.SignUpRequestDto
import io.brieflyz.auth_service.application.dto.TokenResponseDto
import io.brieflyz.auth_service.application.exception.PasswordNotMatchException
import io.brieflyz.auth_service.application.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.application.exception.UserNotFoundException
import io.brieflyz.auth_service.application.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.common.auth.JwtProvider
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.props.AuthServiceProperties
import io.brieflyz.auth_service.common.redis.RedisHandler
import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.repository.MemberRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.thymeleaf.context.Context
import java.security.SecureRandom
import java.time.Year
import java.util.Base64

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val mailProducer: MailProducer,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler,
    private val authServiceProperties: AuthServiceProperties
) {
    @Transactional
    fun join(dto: SignUpRequestDto): Long {
        val (email, password, nickname) = dto
        if (memberRepository.existsByEmail(email)) throw UserAlreadyExistsException("Email: $email")

        val guest = Member.forLocal(email, passwordEncoder.encode(password), nickname)
        memberRepository.save(guest)

        val verifyUrl = authServiceProperties.email.verifyUrl
        val token = generateVerificationToken()
        val ttl = 24 * 3600 * 1000L // 24 hours

        redisHandler.save("VERIFY:$token", email, ttl)

        val context = Context().apply {
            setVariable("email", email)
            setVariable("nickname", nickname)
            setVariable("verifyUrl", "$verifyUrl?token=$token")
            setVariable("unsubscribeUrl", "")
            setVariable("year", Year.now().toString())
        }
        mailProducer.sendAsync(email, context)

        return guest.id
    }

    @Transactional
    fun verifyEmail(token: String) {
        val verifyKey = "VERIFY:$token"
        val email = redisHandler.find(verifyKey)
        val member = findMemberByEmail(email)

        member.updateByEmailVerify() // add USER role

        if (redisHandler.exists(email)) redisHandler.delete(email)
        redisHandler.delete(verifyKey)
    }

    @Transactional(readOnly = true)
    fun login(dto: SignInRequestDto): TokenResponseDto {
        val (email, password) = dto
        val member = findMemberByEmail(email)

        if (member.loginType == LoginType.SOCIAL) throw UserRegisteredBySocialException()
        if (!passwordEncoder.matches(password, member.password)) throw PasswordNotMatchException()

        val username = member.email
        val tokens = jwtProvider.generateToken(username, member.roles)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponseDto(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }

    private fun findMemberByEmail(email: String): Member = memberRepository.findByEmail(email)
        ?: throw UserNotFoundException("Email: $email")

    private fun generateVerificationToken(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}