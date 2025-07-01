package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.infra.db.MemberRepository
import io.brieflyz.auth_service.infra.redis.RedisHandler
import io.brieflyz.auth_service.infra.security.jwt.JwtProvider
import io.brieflyz.auth_service.model.dto.MemberResponseDTO
import io.brieflyz.auth_service.model.dto.TokenResponseDTO
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler
) {
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
        val member = memberRepository.findByEmail(username)
            ?: throw UserNotFoundException("Email: $username")
        deleteRefreshToken(member.email)
        memberRepository.delete(member)
    }

    @Transactional(readOnly = true)
    fun findAllMembers(): List<MemberResponseDTO> = memberRepository.findAll()
        .map { member -> MemberResponseDTO.fromMember(member) }

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): MemberResponseDTO {
        val member = (memberRepository.findByIdOrNull(memberId)
            ?: throw UserNotFoundException("Member ID: $memberId"))
        return MemberResponseDTO.fromMember(member)
    }
}