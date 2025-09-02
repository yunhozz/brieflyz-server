package io.brieflyz.auth_service.domain.service

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.domain.entity.Member
import io.brieflyz.auth_service.domain.exception.PasswordNotMatchException
import io.brieflyz.auth_service.domain.exception.UserAlreadyExistsException
import io.brieflyz.auth_service.domain.exception.UserNotFoundException
import io.brieflyz.auth_service.domain.exception.UserRegisteredBySocialException
import io.brieflyz.auth_service.domain.repository.MemberRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val memberRepository: MemberRepository
) {
    fun saveGuest(email: String, encodedPassword: String, nickname: String): Member {
        if (memberRepository.existsByEmail(email)) throw UserAlreadyExistsException("Email: $email")
        val guest = Member.forLocal(email, encodedPassword, nickname)
        return memberRepository.save(guest)
    }

    fun updateByEmailVerify(email: String) {
        val member = findMemberByEmail(email)
        member.updateByEmailVerify() // add USER role
    }

    fun validatePassword(email: String, password: String, encoder: BCryptPasswordEncoder): Member {
        val member = findMemberByEmail(email)

        if (member.loginType == LoginType.SOCIAL)
            throw UserRegisteredBySocialException()
        if (!encoder.matches(password, member.password))
            throw PasswordNotMatchException()

        return member
    }

    private fun findMemberByEmail(email: String): Member = memberRepository.findByEmail(email)
        ?: throw UserNotFoundException("Email: $email")
}