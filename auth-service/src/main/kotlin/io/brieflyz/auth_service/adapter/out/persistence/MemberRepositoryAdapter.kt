package io.brieflyz.auth_service.adapter.out.persistence

import io.brieflyz.auth_service.adapter.out.persistence.entity.MemberEntity
import io.brieflyz.auth_service.adapter.out.persistence.repository.MemberRepository
import io.brieflyz.auth_service.application.port.out.MemberRepositoryPort
import io.brieflyz.auth_service.domain.model.Member
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MemberRepositoryAdapter(
    private val memberRepository: MemberRepository
) : MemberRepositoryPort {

    override fun save(member: Member): Member {
        val memberEntity = memberRepository.save(member.toEntity())
        return memberEntity.toDomain()
    }

    override fun existsByEmail(email: String): Boolean = memberRepository.existsByEmail(email)

    override fun findMemberById(memberId: Long): Member? {
        val memberEntity = memberRepository.findByIdOrNull(memberId)
        return memberEntity?.toDomain()
    }

    override fun findMemberByEmail(email: String): Member? {
        val memberEntity = memberRepository.findByEmail(email)
        return memberEntity?.toDomain()
    }

    override fun findAllMembers(): List<Member> {
        val memberEntities = memberRepository.findAll()
        return memberEntities.map { it.toDomain() }
    }

    override fun delete(member: Member) {
        memberRepository.delete(member.toEntity())
    }
}

fun Member.toEntity() = MemberEntity(id, email, password, nickname, loginType, roles)

fun MemberEntity.toDomain() = Member.of(id, email, password, nickname, loginType, roles)