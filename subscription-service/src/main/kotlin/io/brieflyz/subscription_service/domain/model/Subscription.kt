package io.brieflyz.subscription_service.domain.model

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import java.time.LocalDateTime

class Subscription private constructor(
    val email: String,
    val country: String,
    val city: String,
    plan: SubscriptionPlan
) {
    companion object {
        fun create(
            email: String,
            country: String,
            city: String,
            plan: SubscriptionPlan,
            id: Long? = null
        ): Subscription {
            val subscription = Subscription(email, country, city, plan)
            id?.let { subscription.id = it }
            return subscription
        }
    }

    var id: Long = 0

    var plan: SubscriptionPlan = plan
        protected set

    var payCount: Int = 0
        protected set

    var deleted: Boolean = false
        protected set

    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set

    fun isSubscriptionPlanEquals(plan: SubscriptionPlan): Boolean = this.plan == plan

    fun isExpired(time: LocalDateTime = LocalDateTime.now()): Boolean = plan.getExpirationTime(updatedAt) <= time

    fun addPayCount() {
        payCount++
    }

    fun delete() {
        require(isActivated())
        deleted = true
    }

    fun isActivated(): Boolean = !deleted

    fun reSubscribe(plan: SubscriptionPlan): Subscription {
        require(!isActivated())
        this.plan = plan
        deleted = false

        return this
    }
}