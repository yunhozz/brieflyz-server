package io.brieflyz.auth_service.config.security

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.model.entity.Member
import io.brieflyz.auth_service.model.security.UserDetailsAdapter
import io.brieflyz.auth_service.repository.MemberRepository
import io.brieflyz.core.utils.logger
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OAuthUserCustomService(
    private val memberRepository: MemberRepository
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val log = logger()

    @Transactional
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val registration = userRequest.clientRegistration

        val oAuthProfile = OAuthProfile.Companion.of(oAuth2User.attributes, registration)
        val email = oAuthProfile.email
        val nickname = oAuthProfile.provider + "_" + oAuthProfile.providerId

        log.debug("OAuth2.0 Profile: {}", oAuthProfile)

        val member = memberRepository.findByEmail(email)?.let { member ->
            when (member.loginType) {
                LoginType.LOCAL -> {
                    log.info("Update Local Member with Social Profile")
                    member.updateBySocialLogin()
                }

                LoginType.SOCIAL -> {
                    log.info("Login by OAuth2.0 Provider: ${oAuthProfile.provider}")
                }
            }
            member

        } ?: run {
            log.info("Create New Member with Social Profile with Nickname: $nickname")
            val socialMember = Member.forSocial(email, nickname)
            memberRepository.save(socialMember)
        }

        return UserDetailsAdapter(member, oAuthProfile.attributes)
    }
}