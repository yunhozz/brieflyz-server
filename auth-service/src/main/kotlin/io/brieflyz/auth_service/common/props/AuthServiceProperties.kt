package io.brieflyz.auth_service.common.props

data class AuthServiceProperties(
    var oauth: OAuthProperties = OAuthProperties(),
    var kafka: KafkaProperties = KafkaProperties(),
    var email: EmailProperties = EmailProperties()
) {
    data class OAuthProperties(
        var authorizationUri: String = "",
        var authorizedRedirectUris: List<String> = emptyList()
    )

    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class EmailProperties(
        var verifyUrl: String = ""
    )
}