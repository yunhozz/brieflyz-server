package io.brieflyz.auth_service.adapter.`in`.web.dto.mapper

import io.brieflyz.auth_service.adapter.`in`.web.dto.response.MemberResponse
import io.brieflyz.auth_service.adapter.`in`.web.dto.response.TokenResponse
import io.brieflyz.auth_service.application.dto.result.MemberResult
import io.brieflyz.auth_service.application.dto.result.TokenResult

fun MemberResult.toResponse() = MemberResponse(id, email, nickname, loginType, roles)

fun List<MemberResult>.toResponse() = this.map { dto ->
    MemberResponse(dto.id, dto.email, dto.nickname, dto.loginType, dto.roles)
}

fun TokenResult.toResponse() = TokenResponse(accessToken, accessTokenValidTime)