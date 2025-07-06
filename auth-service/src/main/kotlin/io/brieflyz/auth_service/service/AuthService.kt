package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.infra.db.MemberRepository
import io.brieflyz.auth_service.infra.redis.RedisHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtProvider
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.model.entity.Member
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler
) {
    @Transactional
    fun join(dto: SignUpRequestDTO): Long {
        val (email, password, nickname) = dto

        if (memberRepository.existsByEmail(email)) throw UserAlreadyExistsException("Email: $email")

        val guest = Member.forLocal(email, passwordEncoder.encode(password), nickname)
        memberRepository.save(guest)

        return guest.id
    }

    @Transactional(readOnly = true)
    fun login(dto: SignInRequestDTO): TokenResponseDTO {
        val (email, password) = dto
        val member = memberRepository.findByEmail(email)
            ?: throw UserNotFoundException("Email: $email")

        if (member.loginType == LoginType.SOCIAL) throw UserRegisteredBySocialException()
        if (!passwordEncoder.matches(password, member.password)) throw PasswordNotMatchException()

        val username = member.email
        val tokens = jwtProvider.generateToken(username, member.roles)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponseDTO(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }
}