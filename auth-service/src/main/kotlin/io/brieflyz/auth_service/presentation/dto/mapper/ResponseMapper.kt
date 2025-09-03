package io.brieflyz.auth_service.presentation.dto.mapper

import io.brieflyz.auth_service.application.dto.MemberResult
import io.brieflyz.auth_service.application.dto.TokenResult
import io.brieflyz.auth_service.presentation.dto.response.MemberResponse
import io.brieflyz.auth_service.presentation.dto.response.TokenResponse

fun MemberResult.toResponse() = MemberResponse(id, email, nickname, loginType, roles)

fun List<MemberResult>.toResponse() = this.map { dto ->
    MemberResponse(dto.id, dto.email, dto.nickname, dto.loginType, dto.roles)
}

fun TokenResult.toResponse() = TokenResponse(accessToken, accessTokenValidTime)