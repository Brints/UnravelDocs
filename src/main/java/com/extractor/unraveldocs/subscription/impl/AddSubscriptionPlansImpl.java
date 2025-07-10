package com.extractor.unraveldocs.subscription.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.extractor.unraveldocs.subscription.dto.response.SubscriptionPlansData;
import com.extractor.unraveldocs.subscription.interfaces.AddSubscriptionPlansService;
import com.extractor.unraveldocs.subscription.model.SubscriptionPlan;
import com.extractor.unraveldocs.subscription.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddSubscriptionPlansImpl implements AddSubscriptionPlansService {
    private final ResponseBuilderService responseBuilderService;
    private final SubscriptionPlanRepository planRepository;

    @Override
    @Transactional
    public UnravelDocsDataResponse<SubscriptionPlansData> createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        if (planRepository.findByName(request.name()).isPresent()) {
            throw new BadRequestException("Subscription plan with this name already exists.");
        }

        SubscriptionPlan newPlan = new SubscriptionPlan();
        newPlan.setName(request.name());
        newPlan.setPrice(request.price());
        newPlan.setCurrency(request.currency());
        newPlan.setBillingIntervalUnit(request.billingIntervalUnit());
        newPlan.setBillingIntervalValue(request.billingIntervalValue());
        newPlan.setDocumentUploadLimit(request.documentUploadLimit());
        newPlan.setOcrPageLimit(request.ocrPageLimit());
        newPlan.setActive(true);

        SubscriptionPlan savedPlan = planRepository.save(newPlan);

        SubscriptionPlansData plansData = getSubscriptionPlansData(savedPlan);

        return responseBuilderService
                .buildUserResponse(plansData, HttpStatus.CREATED, "Subscription plan created successfully.");
    }

    public static SubscriptionPlansData getSubscriptionPlansData(SubscriptionPlan savedPlan) {
        SubscriptionPlansData plansData = new SubscriptionPlansData();
        plansData.setId(savedPlan.getId());
        plansData.setPlanName(savedPlan.getName());
        plansData.setPlanPrice(savedPlan.getPrice());
        plansData.setPlanCurrency(savedPlan.getCurrency());
        plansData.setBillingIntervalUnit(savedPlan.getBillingIntervalUnit());
        plansData.setBillingIntervalValue(savedPlan.getBillingIntervalValue());
        plansData.setDocumentUploadLimit(savedPlan.getDocumentUploadLimit());
        plansData.setOcrPageLimit(savedPlan.getOcrPageLimit());
        plansData.setActive(savedPlan.isActive());
        plansData.setCreatedAt(savedPlan.getCreatedAt());
        plansData.setUpdatedAt(savedPlan.getUpdatedAt());
        return plansData;
    }
}
