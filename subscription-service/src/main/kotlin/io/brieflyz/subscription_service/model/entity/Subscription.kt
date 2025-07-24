package io.brieflyz.subscription_service.model.entity

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import io.brieflyz.subscription_service.common.exception.SubscriptionPlanIdenticalException
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(indexes = [Index(name = "idx_memberId", columnList = "memberId")])
class Subscription(
    val memberId: Long,
    val email: String,
    val country: String,
    val city: String,
    plan: SubscriptionPlan
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Enumerated(EnumType.STRING)
    var plan: SubscriptionPlan = plan
        protected set

    var payCount: Int = 0
        protected set

    var deleted: Boolean = false
        protected set

    fun isSubscriptionPlanEquals(plan: SubscriptionPlan): Boolean = this.plan == plan

    fun updateSubscriptionPlan(plan: SubscriptionPlan) {
        require(this.plan != plan) {
            throw SubscriptionPlanIdenticalException()
        }
        this.plan = plan
    }

    fun isExpired(time: LocalDateTime): Boolean = plan.getExpirationTime(updatedAt!!) <= time

    fun addPayCount() {
        payCount++
    }

    fun delete() {
        require(!deleted)
        deleted = true
    }

    fun isActivated(): Boolean = !deleted

    fun activate(): Subscription {
        require(deleted)
        deleted = false
        return this
    }
}