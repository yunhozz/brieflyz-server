package io.brieflyz.auth_service.application.dto

import io.brieflyz.auth_service.common.constants.LoginType

data class MemberResponseDto(
    val id: Long,
    val email: String,
    val nickname: String,
    val loginType: LoginType,
    val roles: List<String>
)