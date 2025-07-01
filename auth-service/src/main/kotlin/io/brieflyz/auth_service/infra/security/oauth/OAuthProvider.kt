package io.brieflyz.auth_service.infra.security.oauth

enum class OAuthProvider(
    val provider: String
) {
    GOOGLE("google"),
    KAKAO("kakao"),
    NAVER("naver")
    ;

    companion object {
        fun of(provider: String): OAuthProvider? = entries.find { it.provider == provider }
    }
}