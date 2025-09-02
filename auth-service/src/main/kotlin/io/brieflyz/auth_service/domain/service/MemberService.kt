package io.brieflyz.auth_service.domain.service

import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberRepository: MemberRepository
) {
    fun findAllMembers(): List<Member> = memberRepository.findAll()

    fun findMember(id: Long): Member = memberRepository.findById(id)
        ?: throw UserNotFoundException("Member ID: $id")

    fun deleteMember(email: String) {
        val member = findMemberByEmail(email)
        memberRepository.delete(member)
    }

    fun updateBySubscription(email: String, isCreated: Boolean) {
        val member = findMemberByEmail(email)
        member.updateBySubscription(isCreated)
    }

    private fun findMemberByEmail(email: String) = memberRepository.findByEmail(email)
        ?: throw UserNotFoundException("Email: $email")
}