package io.brieflyz.auth_service.model.dto

import io.brieflyz.auth_service.model.entity.Member

data class MemberResponseDTO private constructor(
    val id: Long,
    val email: String,
    val nickname: String,
    val loginType: String,
    val roles: List<String>
) {
    companion object {
        fun fromMember(member: Member) = MemberResponseDTO(
            member.id,
            member.email,
            member.nickname,
            member.loginType.name,
            member.getRoles()
        )
    }
}