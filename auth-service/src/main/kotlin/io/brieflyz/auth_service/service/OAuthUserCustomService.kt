package io.brieflyz.auth_service.service

import io.brieflyz.auth_service.common.constants.LoginType
import io.brieflyz.auth_service.model.entity.Member
import io.brieflyz.auth_service.model.security.UserDetailsAdapter
import io.brieflyz.auth_service.repository.MemberRepository
import io.brieflyz.core.utils.logger
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
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
            val socialMember = Member.Companion.forSocial(email, nickname)
            memberRepository.save(socialMember)
        }

        return UserDetailsAdapter(member, oAuthProfile.attributes)
    }

    data class OAuthProfile private constructor(
        val provider: String,
        val providerId: String,
        val email: String,
        val name: String,
        val imageUrl: String,
        val userNameAttributeName: String,
        val attributes: Map<String, Any>
    ) {
        companion object {
            fun of(attributes: Map<String, Any>, registration: ClientRegistration): OAuthProfile {
                val userNameAttributeName = registration.providerDetails.userInfoEndpoint.userNameAttributeName
                val provider = registration.registrationId

                return when (OAuthProvider.of(provider)) {
                    OAuthProvider.GOOGLE -> ofGoogle(provider, userNameAttributeName, attributes)
                    OAuthProvider.KAKAO -> ofKakao(provider, userNameAttributeName, attributes)
                    OAuthProvider.NAVER -> ofNaver(provider, userNameAttributeName, attributes)
                }
            }

            private fun ofGoogle(
                provider: String,
                userNameAttributeName: String,
                attributes: Map<String, Any>
            ) = OAuthProfile(
                provider,
                attributes["sub"] as String,
                attributes["email"] as String,
                attributes["name"] as String,
                attributes["picture"] as String,
                userNameAttributeName,
                attributes
            )

            private fun ofKakao(
                provider: String,
                userNameAttributeName: String,
                attributes: Map<String, Any>
            ): OAuthProfile {
                val account = attributes["kakao_account"] as Map<String, Any>
                val profile = account["profile"] as Map<String, Any>
                return OAuthProfile(
                    provider,
                    attributes["id"].toString(),
                    account["email"] as String,
                    profile["nickname"] as String,
                    profile["profile_image_url"] as String,
                    userNameAttributeName,
                    attributes
                )
            }

            private fun ofNaver(
                provider: String,
                userNameAttributeName: String,
                attributes: Map<String, Any>
            ): OAuthProfile {
                val response = attributes["response"] as Map<String, Any>
                return OAuthProfile(
                    provider,
                    response["id"] as String,
                    response["email"] as String,
                    response["name"] as String,
                    response["profile_image"] as String,
                    userNameAttributeName,
                    attributes
                )
            }
        }

        private enum class OAuthProvider(
            private val provider: String
        ) {
            GOOGLE("google"),
            KAKAO("kakao"),
            NAVER("naver")
            ;

            companion object {
                fun of(provider: String): OAuthProvider = entries.find { it.provider == provider }
                    ?: throw IllegalArgumentException("This provider $provider does not provide OAuth2.0")
            }
        }
    }
}