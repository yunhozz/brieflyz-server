package io.brieflyz.subscription_service.common.constants

import java.time.LocalDateTime

enum class SubscriptionPlan(val displayName: String) {
    ONE_MONTH("1개월") {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = time.plusMonths(1)
    },
    ONE_YEAR("1년") {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = time.plusYears(1)
    },
    UNLIMITED("무제한") {
        override fun getExpirationTime(time: LocalDateTime): LocalDateTime = LocalDateTime.MAX
    }

    ;

    companion object {
        fun of(plan: String): SubscriptionPlan = entries.find { it.name == plan }
            ?: throw IllegalArgumentException("Unknown Subscription Plan : $plan")
    }

    abstract fun getExpirationTime(time: LocalDateTime): LocalDateTime
}