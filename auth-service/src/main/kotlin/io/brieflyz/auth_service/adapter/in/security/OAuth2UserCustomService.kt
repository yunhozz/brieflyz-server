package io.brieflyz.auth_service.adapter.`in`.security

import io.brieflyz.auth_service.application.dto.command.SocialLoginCommand
import io.brieflyz.auth_service.application.port.`in`.SocialLoginUseCase
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

@Component
class OAuth2UserCustomService(
    private val socialLoginUseCase: SocialLoginUseCase
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val delegate = DefaultOAuth2UserService()

    class UserDetailsAdapter(
        email: String,
        roles: List<String>,
        attributes: Map<String, Any>? = null
    ) : CustomUserDetails(email, roles, attributes)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = delegate.loadUser(userRequest)
        val registration = userRequest.clientRegistration

        val userNameAttributeName = registration.providerDetails.userInfoEndpoint.userNameAttributeName
        val provider = registration.registrationId

        val command = SocialLoginCommand.of(oAuth2User.attributes, userNameAttributeName, provider)
        val result = socialLoginUseCase.loginOrRegisterSocialUser(command)

        return UserDetailsAdapter(result.email, result.roles, result.attributes)
    }
}