package io.brieflyz.auth_service.infra.security.user

import org.springframework.security.core.GrantedAuthority
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
    override fun getUsername(): String = username
    override fun getPassword(): String? = password
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        roles.split("|").map { role ->
            SimpleGrantedAuthority(role)
        }.toMutableSet()

    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    // OAuth2User
    override fun getName(): String = username
    override fun getAttributes(): Map<String, Any>? = attributes
}