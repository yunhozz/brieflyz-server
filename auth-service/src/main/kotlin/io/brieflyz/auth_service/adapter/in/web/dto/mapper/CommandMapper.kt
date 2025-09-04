package io.brieflyz.auth_service.adapter.`in`.web.dto.mapper

import io.brieflyz.auth_service.adapter.`in`.web.dto.request.SignInRequest
import io.brieflyz.auth_service.adapter.`in`.web.dto.request.SignUpRequest
import io.brieflyz.auth_service.application.dto.command.SignInCommand
import io.brieflyz.auth_service.application.dto.command.SignUpCommand

fun SignUpRequest.toCommand() = SignUpCommand(email, password, nickname)

fun SignInRequest.toCommand() = SignInCommand(email, password)