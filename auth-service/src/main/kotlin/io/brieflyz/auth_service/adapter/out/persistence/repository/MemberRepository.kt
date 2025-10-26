package io.brieflyz.auth_service.adapter.out.persistence.repository

import io.brieflyz.auth_service.adapter.out.persistence.entity.MemberEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<MemberEntity, Long> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): MemberEntity?
}