package io.brieflyz.auth_service.application.port.`in`

import io.brieflyz.auth_service.application.dto.command.SocialLoginCommand
import io.brieflyz.auth_service.application.dto.result.SocialUserResult
import io.brieflyz.auth_service.application.dto.result.TokenResult

interface SocialLoginUseCase {
    fun loginOrRegisterSocialUser(command: SocialLoginCommand): SocialUserResult
}

interface OAuthAuthenticateSuccessUseCase {
    fun handleSuccess(username: String, roles: List<String>): TokenResult
}