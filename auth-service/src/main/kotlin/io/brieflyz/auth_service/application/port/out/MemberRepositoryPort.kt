package io.brieflyz.auth_service.application.port.out

import io.brieflyz.auth_service.domain.model.Member

interface MemberRepositoryPort {
    fun save(member: Member): Member
    fun existsByEmail(email: String): Boolean
    fun findMemberById(memberId: Long): Member?
    fun findMemberByEmail(email: String): Member?
    fun findAllMembers(): List<Member>
    fun delete(member: Member)
}