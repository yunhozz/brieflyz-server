package io.brieflyz.subscription_service.repository

import io.brieflyz.subscription_service.model.entity.PaymentDetails
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentDetailsRepository : JpaRepository<PaymentDetails, Long>