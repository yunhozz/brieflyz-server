package io.brieflyz.auth_service.application.service

import io.brieflyz.auth_service.application.dto.result.MemberResult
import io.brieflyz.auth_service.application.dto.result.TokenResult
import io.brieflyz.auth_service.application.port.`in`.DeleteRefreshTokenUseCase
import io.brieflyz.auth_service.application.port.`in`.FindAllMembersUseCase
import io.brieflyz.auth_service.application.port.`in`.FindMemberUseCase
import io.brieflyz.auth_service.application.port.`in`.TokenRefreshUseCase
import io.brieflyz.auth_service.application.port.`in`.UpdateSubscriptionStatusUseCase
import io.brieflyz.auth_service.application.port.`in`.WithdrawUseCase
import io.brieflyz.auth_service.application.port.out.CachePort
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.application.port.out.TokenProviderPort
import io.brieflyz.auth_service.common.exception.RefreshTokenNotFoundException
import io.brieflyz.auth_service.common.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.model.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FindMemberService(
    private val memberRepositoryPort: MemberRepositoryPort
) : FindMemberUseCase {

    @Transactional(readOnly = true)
    override fun findMemberById(memberId: Long): MemberResult {
        val member = memberRepositoryPort.findMemberById(memberId)
            ?: throw UserNotFoundException("Member ID=$memberId")
        return member.toResult()
    }
}

@Service
class FindAllMembersService(
    private val memberRepositoryPort: MemberRepositoryPort
) : FindAllMembersUseCase {

    @Transactional(readOnly = true)
    override fun findAllMembers(): List<MemberResult> = memberRepositoryPort.findAllMembers()
        .map { member -> member.toResult() }
}

@Service
class TokenRefreshService(
    private val tokenProviderPort: TokenProviderPort,
    private val cachePort: CachePort
) : TokenRefreshUseCase {

    override fun refresh(username: String): TokenResult {
        if (!cachePort.exists(username)) throw RefreshTokenNotFoundException() // re-login

        val refreshToken = cachePort.find(username)
            ?: throw IllegalArgumentException("Cache data does not exist. Key=$username")
        val principal = tokenProviderPort.getPrincipal(refreshToken)
        val tokenResult = tokenProviderPort.generateToken(principal.username, principal.roles.joinToString("|"))

        cachePort.save(username, tokenResult.refreshToken, tokenResult.refreshTokenValidTime)

        return tokenResult
    }
}

@Service
class DeleteRefreshTokenService(
    private val cachePort: CachePort
) : DeleteRefreshTokenUseCase {

    override fun delete(username: String) {
        cachePort.delete(username)
    }
}

@Service
class WithdrawService(
    private val memberRepositoryPort: MemberRepositoryPort,
    private val cachePort: CachePort
) : WithdrawUseCase {

    @Transactional
    override fun withdraw(username: String) {
        val member = memberRepositoryPort.findMemberByEmail(username)
            ?: throw UserNotFoundException("Email=$username")
        memberRepositoryPort.delete(member)
        cachePort.delete(username)
    }
}

@Service
class UpdateSubscriptionStatusService(
    private val memberRepositoryPort: MemberRepositoryPort
) : UpdateSubscriptionStatusUseCase {

    @Transactional
    override fun updateBySubscriptionStatus(email: String, isCreated: Boolean) {
        val member = memberRepositoryPort.findMemberByEmail(email)
            ?: throw UserNotFoundException("Email=$email")
        member.updateBySubscription(isCreated)
    }
}

private fun Member.toResult() = MemberResult(
    id = this.id,
    email = this.email,
    nickname = this.email,
    loginType = this.loginType,
    roles = this.getRoles()
)