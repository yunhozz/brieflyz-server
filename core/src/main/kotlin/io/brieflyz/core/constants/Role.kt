package io.brieflyz.core.constants

enum class Role(val auth: String) {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN")
}