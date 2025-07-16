package com.extractor.unraveldocs.subscription.dto.request;

import com.extractor.unraveldocs.subscription.enums.BillingIntervalUnit;
import com.extractor.unraveldocs.subscription.enums.SubscriptionCurrency;
import com.extractor.unraveldocs.subscription.enums.SubscriptionPlans;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreateSubscriptionPlanRequest(
        @NotNull(message = "Subscription plan name is required")
        SubscriptionPlans name,

        @NotNull(message = "Subscription plan price is required")
        @Min(value = 0, message = "Subscription plan price must be a positive value")
        BigDecimal price,

        @NotNull(message = "Currency is required")
        SubscriptionCurrency currency,

        @NotNull(message = "Billing interval unit is required")
        BillingIntervalUnit billingIntervalUnit,

        @NotNull(message = "Billing interval value is required")
        @Min(value = 1, message = "Billing interval value must be at least 1")
        Integer billingIntervalValue,

        @NotNull(message = "Document upload limit is required")
        Integer documentUploadLimit,

        @NotNull(message = "OCR page limit is required")
        Integer ocrPageLimit
) {}
