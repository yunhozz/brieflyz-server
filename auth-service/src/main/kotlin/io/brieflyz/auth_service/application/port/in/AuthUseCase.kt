package io.brieflyz.auth_service.application.port.`in`

import io.brieflyz.auth_service.application.dto.command.SignInCommand
import io.brieflyz.auth_service.application.dto.command.SignUpCommand
import io.brieflyz.auth_service.application.dto.result.TokenResult

interface JoinUseCase {
    fun join(command: SignUpCommand): Long
}

interface LoginUseCase {
    fun login(command: SignInCommand): TokenResult
}

interface VerifyEmailUseCase {
    fun verifyByToken(token: String)
}