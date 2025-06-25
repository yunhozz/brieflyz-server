package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.exception.PasswordNotMatchException
import io.brieflyz.auth_service.common.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.infra.db.MemberRepository
import io.brieflyz.auth_service.infra.security.jwt.JwtProvider
import io.brieflyz.auth_service.infra.security.jwt.JwtTokens
import io.brieflyz.auth_service.infra.security.user.Role
import io.brieflyz.auth_service.model.dto.SignInRequestDTO
import io.brieflyz.auth_service.model.dto.SignUpRequestDTO
import io.brieflyz.auth_service.model.entity.Member
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtProvider: JwtProvider
) {
    @Transactional
    fun join(dto: SignUpRequestDTO): Long? {
        val (email, password) = dto

        if (memberRepository.existsByEmail(email))
            throw UserAlreadyExistsException("Email: $email")

        val guest = Member(email, passwordEncoder.encode(password))
        guest.addRoles(Role.USER, Role.ADMIN) // for test
        memberRepository.save(guest)

        return guest.id
    }

    @Transactional(readOnly = true)
    fun login(dto: SignInRequestDTO): JwtTokens {
        val (email, password) = dto

        memberRepository.findByEmail(email)?.let { member ->
            if (!passwordEncoder.matches(password, member.password))
                throw PasswordNotMatchException()

            return jwtProvider.generateToken(member.email, member.roles)

        } ?: throw UserNotFoundException("Email: $email")
    }

    @Transactional(readOnly = true)
    fun refreshToken(refreshToken: String): JwtTokens {
        val token = refreshToken.split(" ")[1] // TODO: Redis에서 재발급 토큰 조회
        val authentication = jwtProvider.getAuthentication(token)
        return jwtProvider.generateToken(authentication)
    }

    @Transactional(readOnly = true)
    fun findAllMembers(): List<Member> = memberRepository.findAll()

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): Member? = memberRepository.findByIdOrNull(memberId)
        ?: throw UserNotFoundException("Member ID: $memberId")
}