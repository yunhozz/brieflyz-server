package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.model.security.CustomUserDetails
import io.brieflyz.auth_service.model.security.UserDetailsAdapter
import io.brieflyz.auth_service.repository.MemberRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CustomUserDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): CustomUserDetails {
        val member = memberRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("Username: $username")
        return UserDetailsAdapter(member)
    }
}