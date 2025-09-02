package io.brieflyz.auth_service.domain.repository

import io.brieflyz.auth_service.domain.entity.Member

interface MemberRepository {
    fun save(member: Member): Member
    fun existsByEmail(email: String): Boolean
    fun findById(id: Long): Member?
    fun findByEmail(email: String): Member?
    fun findAll(): List<Member>
    fun delete(member: Member)
}