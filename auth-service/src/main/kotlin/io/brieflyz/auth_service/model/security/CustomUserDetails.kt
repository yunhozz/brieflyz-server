package io.brieflyz.auth_service.model.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User

open class CustomUserDetails(
    private val username: String,
    private val password: String?,
    private val roles: String,
    private val attributes: Map<String, Any>? = null
) : UserDetails, OAuth2User {
    // UserDetails
    override fun getUsername() = username
    override fun getPassword() = password
    override fun getAuthorities() = roles.split("|")
        .map { role -> SimpleGrantedAuthority(role) }.toMutableSet()

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true

    // OAuth2User
    override fun getName() = username
    override fun getAttributes() = attributes
}