package io.brieflyz.subscription_service.common.constants

enum class SubscriptionPlan {
    ONE_MONTH,
    ONE_YEAR,
    UNLIMITED
    ;

    companion object {
        fun of(plan: String): SubscriptionPlan = entries.find { it.name == plan }
            ?: throw IllegalArgumentException("Unknown Subscription Plan : $plan")
    }
}