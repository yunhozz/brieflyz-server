package io.brieflyz.subscription_service.common.constants

import io.brieflyz.subscription_service.common.exception.InvalidSubscriptionIntervalException

enum class SubscriptionInterval(private val interval: String) {
    ONE_MONTH("one month"),
    THREE_MONTH("three month"),
    SIX_MONTH("six month"),
    ONE_YEAR("one year")
    ;

    companion object {
        fun of(interval: String): SubscriptionInterval = entries.find { it.interval == interval }
            ?: throw InvalidSubscriptionIntervalException("Subscription Interval: '$interval'")
    }
}