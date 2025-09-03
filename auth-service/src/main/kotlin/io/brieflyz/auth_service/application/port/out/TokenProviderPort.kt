package io.brieflyz.auth_service.application.port.out

import io.brieflyz.auth_service.application.dto.result.PrincipalResult
import io.brieflyz.auth_service.application.dto.result.TokenResult

interface TokenProviderPort {
    fun generateToken(username: String, roles: String): TokenResult
    fun getPrincipal(token: String): PrincipalResult
}