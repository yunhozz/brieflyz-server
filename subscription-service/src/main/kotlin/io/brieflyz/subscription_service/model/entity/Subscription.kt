package io.brieflyz.subscription_service.model.entity

import io.brieflyz.subscription_service.common.constants.SubscriptionPlan
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(indexes = [Index(name = "idx_memberId_email", columnList = "memberId, email")])
@SQLRestriction("deleted = false")
class Subscription(
    val memberId: Long,
    val email: String,
    val country: String,
    val city: String,
    plan: SubscriptionPlan,
    count: Int = 0
) : BaseEntity() {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0

    @Enumerated(EnumType.STRING)
    var plan: SubscriptionPlan = plan
        protected set

    var count: Int = count
        protected set

    var deleted: Boolean = false
        protected set

    fun isSubscriptionPlanEquals(plan: SubscriptionPlan): Boolean = this.plan == plan

    fun updateSubscriptionPlan(plan: SubscriptionPlan) {
        this.plan = plan
    }

    fun addCount() {
        count++
    }

    fun delete() {
        require(!deleted)
        deleted = true
    }
}