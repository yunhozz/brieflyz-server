package io.brieflyz.auth_service.infra.security.jwt

import org.springframework.security.core.GrantedAuthority

enum class Role(private val auth: String) : GrantedAuthority {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
    ;

    override fun getAuthority(): String = auth
}