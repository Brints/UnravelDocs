package com.extractor.unraveldocs.subscription.service;

import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.extractor.unraveldocs.subscription.dto.response.SubscriptionPlansData;
import com.extractor.unraveldocs.subscription.interfaces.SubscriptionPlansService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionPlansService subscriptionPlansService;

    public UnravelDocsDataResponse<SubscriptionPlansData> createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        return subscriptionPlansService.createSubscriptionPlan(request);
    }
}
