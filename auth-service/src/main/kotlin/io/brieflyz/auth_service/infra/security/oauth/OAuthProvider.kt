package io.brieflyz.auth_service.infra.security.oauth

data class OAuthProvider private constructor(
    val email: String,
    val name: String,
    val imageUrl: String,
    val userNameAttributeName: String,
    val attributes: Map<String, Any>
) {
    companion object {
        fun of(
            registrar: OAuthRegistrar,
            userNameAttributeName: String,
            attributes: Map<String, Any>
        ): OAuthProvider =
            when (registrar) {
                OAuthRegistrar.GOOGLE -> ofGoogle(userNameAttributeName, attributes)
                OAuthRegistrar.KAKAO -> ofKakao(userNameAttributeName, attributes)
                OAuthRegistrar.NAVER -> ofNaver(userNameAttributeName, attributes)
            }

        private fun ofGoogle(userNameAttributeName: String, attributes: Map<String, Any>): OAuthProvider =
            OAuthProvider(
                attributes["email"] as String,
                attributes["name"] as String,
                attributes["picture"] as String,
                userNameAttributeName,
                attributes
            )

        private fun ofKakao(userNameAttributeName: String, attributes: Map<String, Any>): OAuthProvider {
            val account = attributes["kakao_account"] as Map<String, Any>
            val profile = attributes["profile"] as Map<String, Any>
            return OAuthProvider(
                account["email"] as String,
                profile["nickname"] as String,
                profile["profile_img_url"] as String,
                userNameAttributeName,
                attributes
            )
        }

        private fun ofNaver(userNameAttributeName: String, attributes: Map<String, Any>): OAuthProvider {
            val response = attributes["response"] as Map<String, Any>
            return OAuthProvider(
                response["email"] as String,
                response["name"] as String,
                response["profile_image"] as String,
                userNameAttributeName,
                attributes
            )
        }
    }
}