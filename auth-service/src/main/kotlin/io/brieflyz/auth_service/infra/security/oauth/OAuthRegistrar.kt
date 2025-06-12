package io.brieflyz.auth_service.infra.security.oauth

enum class OAuthRegistrar(
    val registrationId: String
) {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver")
}