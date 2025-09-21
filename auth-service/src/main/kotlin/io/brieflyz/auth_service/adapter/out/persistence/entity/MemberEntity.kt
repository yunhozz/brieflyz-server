package io.brieflyz.auth_service.adapter.out.persistence.entity

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
@Table(name = "member", indexes = [Index(name = "idx_email", columnList = "email")])
class MemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,
    val email: String,
    val password: String?,
    val nickname: String,
    @Enumerated(EnumType.STRING)
    val loginType: LoginType,
    val roles: String = Role.GUEST.name
) : BaseEntity()