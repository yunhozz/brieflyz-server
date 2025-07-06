package io.brieflyz.auth_service.model.entity

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.common.constants.Role
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class Member private constructor(
    val email: String,
    val password: String?,
    nickname: String,
    loginType: LoginType,
    roles: String = Role.GUEST.authority
) : BaseEntity() {

    companion object {
        fun forLocal(email: String, password: String?, nickname: String) =
            Member(email, password, nickname, LoginType.LOCAL)

        fun forSocial(email: String, nickname: String): Member {
            val member = Member(email, null, nickname, LoginType.SOCIAL)
            member.addRoles(Role.USER)
            return member
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    var nickname: String = nickname
        protected set

    @Enumerated(EnumType.STRING)
    var loginType: LoginType = loginType
        protected set

    var roles: String = roles
        protected set

    fun updateNickname(newNickname: String) {
        require(newNickname != nickname)
        nickname = newNickname
    }

    fun getRoles(): List<String> = roles.split("|").map { role ->
        role.replace("ROLE_", "")
    }

    fun addRoles(vararg newRoles: Role) {
        val newAuthorities = newRoles.joinToString("") { role ->
            val authority = role.authority
            require(!roles.contains(authority)) { "Already Authorized on $authority" }
            "|$authority"
        }
        roles += newAuthorities
    }

    fun updateBySocialLogin() {
        loginType = LoginType.SOCIAL
        if (!roles.contains(Role.USER.authority)) addRoles(Role.USER)
    }

    fun isLoginBy(type: LoginType): Boolean = loginType == type
}