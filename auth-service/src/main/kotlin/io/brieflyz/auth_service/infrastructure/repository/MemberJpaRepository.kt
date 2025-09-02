package io.brieflyz.auth_service.infrastructure.repository

import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.repository.MemberRepository
import org.springframework.data.jpa.repository.JpaRepository

interface MemberJpaRepository : JpaRepository<Member, Long>, MemberRepository