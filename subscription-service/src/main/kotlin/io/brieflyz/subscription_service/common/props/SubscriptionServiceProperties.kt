package io.brieflyz.subscription_service.common.props

data class SubscriptionServiceProperties(
    var kafka: KafkaProperties = KafkaProperties(),
    var email: EmailProperties = EmailProperties()
) {
    data class KafkaProperties(
        var numOfPartitions: Int = 0,
        var replicationFactor: Short = 0
    )

    data class EmailProperties(
        var dashboardUrl: String = "",
        var renewUrl: String = "",
    )
}