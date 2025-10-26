package io.brieflyz.auth_service.application.dto.result

data class SocialUserResult(
    val email: String,
    val roles: List<String>,
    val attributes: Map<String, Any>
)