package io.brieflyz.auth_service.domain.model

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.core.constants.Role

class Member private constructor(
    val id: Long,
    val email: String,
    val password: String?,
    nickname: String,
    loginType: LoginType,
    roles: String = Role.GUEST.name
) {
    companion object {
        fun of(
            id: Long,
            email: String,
            password: String?,
            nickname: String,
            loginType: LoginType,
            roles: String
        ): Member = Member(id, email, password, nickname, loginType, roles)

        fun forLocal(email: String, password: String?, nickname: String) =
            Member(0, email, password, nickname, LoginType.LOCAL)

        fun forSocial(email: String, nickname: String): Member {
            val member = Member(0, email, null, nickname, LoginType.SOCIAL)
            member.addRoles(Role.USER)
            return member
        }
    }

    var nickname: String = nickname
        protected set

    var loginType: LoginType = loginType
        protected set

    var roles: String = roles
        protected set

    fun updateNickname(newNickname: String) {
        require(newNickname != nickname)
        nickname = newNickname
    }

    fun getRoles(): List<String> = roles.split("|")

    fun updateByEmailVerify() {
        addRoles(Role.USER)
    }

    fun updateBySocialLogin() {
        loginType = LoginType.SOCIAL
        addRoles(Role.USER)
    }

    fun updateBySubscription(subscribe: Boolean) {
        if (subscribe) {
            addRoles(Role.MEMBER)
        } else {
            deleteRoles(Role.MEMBER)
        }
    }

    private fun addRoles(vararg roles: Role) {
        val existingRoles = this.roles.split("|").filter { it.isNotBlank() }
        val newRoles = existingRoles + roles.map { it.name }
        this.roles = newRoles.distinct().joinToString("|")
    }

    private fun deleteRoles(vararg roles: Role) {
        val existingRoles = this.roles.split("|").filter { it.isNotBlank() }
        val newRoles = existingRoles - roles.map { it.name }.toSet()
        this.roles = newRoles.distinct().joinToString("|")
    }
}