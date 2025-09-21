package io.brieflyz.subscription_service.adapter.out.persistence.repository

import io.brieflyz.subscription_service.adapter.out.persistence.entity.ExpiredSubscriptionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExpiredSubscriptionRepository : JpaRepository<ExpiredSubscriptionEntity, Long>