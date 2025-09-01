package io.brieflyz.auth_service.model.entity

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.core.constants.Role
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(indexes = [Index(name = "idx_email", columnList = "email")])
class Member private constructor(
    val email: String,
    val password: String?,
    nickname: String,
    loginType: LoginType,
    roles: String = Role.GUEST.name
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
    val id: Long = 0

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