package io.brieflyz.auth_service.infra.security.oauth

import org.springframework.security.oauth2.client.registration.ClientRegistration

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
                null -> TODO()
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
            val profile = attributes["profile"] as Map<String, Any>
            return OAuthProfile(
                provider,
                attributes["sub"] as String,
                account["email"] as String,
                profile["nickname"] as String,
                profile["profile_img_url"] as String,
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
                attributes["sub"] as String,
                response["email"] as String,
                response["name"] as String,
                response["profile_image"] as String,
                userNameAttributeName,
                attributes
            )
        }
    }
}