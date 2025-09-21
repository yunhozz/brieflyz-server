package io.brieflyz.auth_service.application.port.`in`

import io.brieflyz.auth_service.application.dto.result.MemberResult
import io.brieflyz.auth_service.application.dto.result.TokenResult

interface FindMemberUseCase {
    fun findMemberById(memberId: Long): MemberResult
}

interface FindAllMembersUseCase {
    fun findAllMembers(): List<MemberResult>
}

interface TokenRefreshUseCase {
    fun refresh(username: String): TokenResult
}

interface DeleteRefreshTokenUseCase {
    fun delete(username: String)
}

interface WithdrawUseCase {
    fun withdraw(username: String)
}

interface UpdateSubscriptionStatusUseCase {
    fun updateBySubscriptionStatus(email: String, isCreated: Boolean)
}