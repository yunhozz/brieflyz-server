package io.brieflyz.auth_service.model.entity

import io.brieflyz.auth_service.infra.security.user.Role
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Member(
    val email: String,
    val password: String,
    roles: String = Role.GUEST.authority
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var roles: String = roles
        protected set

    fun getRoles(): List<String> = roles.split("|").map { role ->
        role.replace("ROLE_", "")
    }

    fun addRoles(vararg newRoles: Role) {
        newRoles.forEach { role ->
            val authority = role.authority
            require(!roles.contains(authority)) { "Already Authorized on $authority" }
            roles += "|$authority"
        }
    }
}