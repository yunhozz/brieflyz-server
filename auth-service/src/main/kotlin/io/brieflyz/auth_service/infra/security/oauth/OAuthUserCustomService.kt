package io.brieflyz.auth_service.infra.security.oauth

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

@Component
class OAuthUserCustomService : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        requireNotNull(userRequest) { "OAuth2UserRequest must not be null" }

        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val registration = userRequest.clientRegistration

        val attributes = oAuth2User.attributes
        val registrationId = registration.registrationId
        val userNameAttributeName = registration.providerDetails.userInfoEndpoint.userNameAttributeName

        val provider = OAuthProvider.of(
            registrar = OAuthRegistrar.valueOf(registrationId),
            userNameAttributeName,
            attributes
        )

        // TODO: JDBC or Redis 에 정보 저장

        return oAuth2User
    }
}