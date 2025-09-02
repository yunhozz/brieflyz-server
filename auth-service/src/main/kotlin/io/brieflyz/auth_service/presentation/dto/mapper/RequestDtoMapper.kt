package io.brieflyz.auth_service.presentation.dto.mapper

import io.brieflyz.auth_service.application.dto.SignInRequestDto
import io.brieflyz.auth_service.application.dto.SignUpRequestDto
import io.brieflyz.auth_service.presentation.dto.request.SignInRequest
import io.brieflyz.auth_service.presentation.dto.request.SignUpRequest

fun SignUpRequest.toDto() = SignUpRequestDto(email, password, nickname)

fun SignInRequest.toDto() = SignInRequestDto(email, password)