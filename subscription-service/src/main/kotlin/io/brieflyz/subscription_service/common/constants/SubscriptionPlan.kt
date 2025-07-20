package io.brieflyz.subscription_service.common.constants

enum class SubscriptionPlan(
    private val plan: String
) {
    ONE_MONTH("one month"),
    THREE_MONTH("three month"),
    SIX_MONTH("six month"),
    ONE_YEAR("one year")
    ;

    companion object {
        fun of(plan: String): SubscriptionPlan = entries.find { it.plan == plan }
            ?: throw IllegalArgumentException("Subscription Plan: '$plan'")
    }
}