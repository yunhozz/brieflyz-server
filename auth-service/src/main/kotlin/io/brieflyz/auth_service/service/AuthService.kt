package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.component.JwtProvider
import io.brieflyz.auth_service.common.component.RedisHandler
import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.model.dto.request.SignInRequest
import io.brieflyz.auth_service.model.dto.request.SignUpRequest
import io.brieflyz.auth_service.model.dto.response.TokenResponse
import io.brieflyz.auth_service.model.entity.Member
import io.brieflyz.auth_service.repository.MemberRepository
import io.brieflyz.auth_service.service.support.MailProducer
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
    private val redisHandler: RedisHandler
) {
    @Transactional
    fun join(request: SignUpRequest): Long {
        val (email, password, nickname) = request

        if (memberRepository.existsByEmail(email)) throw UserAlreadyExistsException("Email: $email")

        val guest = Member.forLocal(email, passwordEncoder.encode(password), nickname)
        memberRepository.save(guest)

        val token = generateVerificationToken()
        val ttl = 24 * 3600 * 1000L // 24 hours
        redisHandler.save("VERIFY:$token", email, ttl)

        val context = Context().apply {
            setVariable("email", email)
            setVariable("nickname", nickname)
            setVariable("verifyUrl", "http://localhost:8000/api/auth/verify?token=$token")
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

        member.updateByEmailVerify()
        redisHandler.delete(verifyKey)
    }

    @Transactional(readOnly = true)
    fun login(request: SignInRequest): TokenResponse {
        val (email, password) = request
        val member = findMemberByEmail(email)

        if (member.loginType == LoginType.SOCIAL) throw UserRegisteredBySocialException()
        if (!passwordEncoder.matches(password, member.password)) throw PasswordNotMatchException()

        val username = member.email
        val tokens = jwtProvider.generateToken(username, member.roles)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponse(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
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