package com.extractor.unraveldocs.subscription.interfaces;

import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.extractor.unraveldocs.subscription.dto.response.SubscriptionPlansData;

public interface SubscriptionPlansService {
    UnravelDocsDataResponse<SubscriptionPlansData> createSubscriptionPlan(CreateSubscriptionPlanRequest request);
}
