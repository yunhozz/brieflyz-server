package io.brieflyz.auth_service.application.dto.result

data class PrincipalResult(
    val username: String,
    val roles: List<String>
)