package io.brieflyz.auth_service.presentation.dto.mapper

import io.brieflyz.auth_service.application.dto.MemberResponseDto
import io.brieflyz.auth_service.application.dto.TokenResponseDto
import io.brieflyz.auth_service.presentation.dto.response.MemberResponse
import io.brieflyz.auth_service.presentation.dto.response.TokenResponse

fun MemberResponseDto.toResponse() = MemberResponse(id, email, nickname, loginType, roles)

fun List<MemberResponseDto>.toResponse() = this.map { dto ->
    MemberResponse(dto.id, dto.email, dto.nickname, dto.loginType, dto.roles)
}

fun TokenResponseDto.toResponse() = TokenResponse(accessToken, accessTokenValidTime)