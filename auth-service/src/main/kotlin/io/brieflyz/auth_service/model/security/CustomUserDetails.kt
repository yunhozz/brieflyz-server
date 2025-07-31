package io.brieflyz.auth_service.model.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

open class CustomUserDetails(
    val username: String,
    private val roles: List<String>,
    private val attributes: Map<String, Any>? = null
) : OAuth2User {
    override fun getName() = username
    override fun getAuthorities() = roles
        .map { role -> SimpleGrantedAuthority(role) }.toMutableSet()

    override fun getAttributes() = attributes
}