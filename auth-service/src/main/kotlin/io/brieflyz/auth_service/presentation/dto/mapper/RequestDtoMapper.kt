package io.brieflyz.auth_service.presentation.dto.mapper

import io.brieflyz.auth_service.application.dto.SignInCommand
import io.brieflyz.auth_service.application.dto.SignUpCommand
import io.brieflyz.auth_service.presentation.dto.request.SignInRequest
import io.brieflyz.auth_service.presentation.dto.request.SignUpRequest

fun SignUpRequest.toCommand() = SignUpCommand(email, password, nickname)

fun SignInRequest.toCommand() = SignInCommand(email, password)