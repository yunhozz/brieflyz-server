package io.brieflyz.auth_service.model.dto

import io.brieflyz.auth_service.model.entity.Member

data class MemberResponseDTO private constructor(
    val email: String,
    val roles: List<String>
) {
    companion object {
        fun of(member: Member): MemberResponseDTO = MemberResponseDTO(
            member.email,
            member.getRoles()
        )
    }
}