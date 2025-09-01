package io.brieflyz.core.constants

enum class Role(val roleName: String) {
    GUEST("ROLE_GUEST"),
    USER("ROLE_USER"),
    MEMBER("ROLE_MEMBER"),
    ADMIN("ROLE_ADMIN")
    ;

    companion object {
        fun of(role: String) = entries.find { it.name == role }
            ?: throw IllegalArgumentException("Role does not exist. Role=$role")
    }
}