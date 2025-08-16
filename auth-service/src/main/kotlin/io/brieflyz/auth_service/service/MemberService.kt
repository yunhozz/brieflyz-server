package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.common.infra.redis.RedisHandler
import io.brieflyz.auth_service.common.security.JwtProvider
import io.brieflyz.auth_service.model.dto.response.MemberResponse
import io.brieflyz.auth_service.model.dto.response.TokenResponse
import io.brieflyz.auth_service.model.entity.Member
import io.brieflyz.auth_service.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider,
    private val redisHandler: RedisHandler
) {
    fun refreshToken(username: String): TokenResponse {
        if (!redisHandler.exists(username)) throw RefreshTokenNotFoundException() // re-login

        val refreshToken = redisHandler.find(username)
        val authentication = jwtProvider.getAuthentication(refreshToken)
        val tokens = jwtProvider.generateToken(authentication)

        redisHandler.save(username, tokens.refreshToken, tokens.refreshTokenValidTime)

        return TokenResponse(tokens.tokenType + tokens.accessToken, tokens.accessTokenValidTime)
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
    fun findAllMembers(): List<MemberResponse> = memberRepository.findAll()
        .map { member -> member.toResponse() }

    @Transactional(readOnly = true)
    fun findMemberById(memberId: Long): MemberResponse {
        val member = (memberRepository.findByIdOrNull(memberId)
            ?: throw UserNotFoundException("Member ID: $memberId"))
        return member.toResponse()
    }

    private fun Member.toResponse() = MemberResponse(
        id = this.id,
        email = this.email,
        nickname = this.email,
        loginType = this.loginType,
        roles = this.getRoles()
    )
}