package io.brieflyz.auth_service.presentation.dto.response

import io.brieflyz.auth_service.common.constants.LoginType

data class MemberResponse(
    val id: Long,
    val email: String,
    val nickname: String,
    val loginType: LoginType,
    val roles: List<String>
)