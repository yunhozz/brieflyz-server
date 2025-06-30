package io.brieflyz.auth_service.infra.security.oauth

import io.brieflyz.auth_service.infra.db.MemberRepository
import io.brieflyz.auth_service.infra.security.user.UserDetailsAdapter
import io.brieflyz.auth_service.model.entity.Member
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

        val oAuthProfile = OAuthProfile.of(oAuth2User.attributes, registration)
        val email = oAuthProfile.email
        val nickname = oAuthProfile.provider + "_" + oAuthProfile.providerId

        log.debug("OAuth2 Profile: {}", oAuthProfile)

        val member = memberRepository.findByEmail(email)?.let { localMember ->
            localMember.updateBySocialLogin()
            localMember
        } ?: run {
            val socialMember = Member.forSocial(email, nickname)
            memberRepository.save(socialMember)
        }

        return UserDetailsAdapter(member, oAuthProfile.attributes)
    }
}