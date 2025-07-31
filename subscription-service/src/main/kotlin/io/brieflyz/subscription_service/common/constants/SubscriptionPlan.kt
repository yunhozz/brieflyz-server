package io.brieflyz.subscription_service.common.constants

import java.time.LocalDateTime

enum class SubscriptionPlan {
    ONE_MONTH {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = time.plusMonths(1)
    },
    ONE_YEAR {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = time.plusYears(1)
    },
    UNLIMITED {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = LocalDateTime.MAX
    }

    ;

    companion object {
        fun of(plan: String): SubscriptionPlan = entries.find { it.name == plan }
            ?: throw IllegalArgumentException("Unknown Subscription Plan : $plan")
    }

    abstract fun getExpirationTime(time: LocalDateTime): LocalDateTime
}