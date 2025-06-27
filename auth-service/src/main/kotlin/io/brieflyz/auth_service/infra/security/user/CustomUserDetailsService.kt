package io.brieflyz.auth_service.infra.security.user

import io.brieflyz.auth_service.infra.db.MemberRepository
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