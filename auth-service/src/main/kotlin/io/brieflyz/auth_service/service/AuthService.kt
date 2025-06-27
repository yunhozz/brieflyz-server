package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.infra.db.MemberRepository
import io.brieflyz.auth_service.infra.redis.RedisHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtProvider
import io.brieflyz.auth_service.infra.security.user.Role
import io.brieflyz.auth_service.model.dto.MemberResponseDTO
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import io.brieflyz.auth_service.model.entity.Member
import org.springframework.data.repository.findByIdOrNull
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
        val (email, password) = dto

        if (memberRepository.existsByEmail(email)) throw UserAlreadyExistsException("Email: $email")

        val guest = Member(email, passwordEncoder.encode(password))
        guest.addRoles(Role.USER, Role.ADMIN) // for test
        memberRepository.save(guest)

        return guest.id
    }

    @Transactional(readOnly = true)
    fun login(dto: SignInRequestDTO): TokenResponseDTO {
        val (email, password) = dto
        val member = findMemberByEmail(email)

        if (!passwordEncoder.matches(password, member.password)) throw PasswordNotMatchException()

        val username = member.email
        val tokens = jwtProvider.generateToken(username, member.roles)
        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponseDTO(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }

    fun refreshToken(username: String): TokenResponseDTO {
        if (!redisHandler.exists(username)) throw RefreshTokenNotFoundException() // re-login

        val refreshToken = redisHandler.find(username)
        val authentication = jwtProvider.getAuthentication(refreshToken)
        val tokens = jwtProvider.generateToken(authentication)
        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponseDTO(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
    }

    fun deleteRefreshToken(username: String) {
        redisHandler.delete(username)
    }

    @Transactional
    fun withdraw(username: String) {
        val member = findMemberByEmail(username)
        deleteRefreshToken(member.email)
        memberRepository.delete(member)
    }

    @Transactional(readOnly = true)
    fun findAllMembers(): List<MemberResponseDTO> = memberRepository.findAll()
        .map { member -> MemberResponseDTO.of(member) }

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): MemberResponseDTO {
        val member = (memberRepository.findByIdOrNull(memberId)
            ?: throw UserNotFoundException("Member ID: $memberId"))
        return MemberResponseDTO.of(member)
    }

    private fun findMemberByEmail(email: String): Member = memberRepository.findByEmail(email)
        ?: throw UserNotFoundException("Email: $email")
}