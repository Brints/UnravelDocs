package com.extractor.unraveldocs.subscription.impl;

import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.subscription.enums.SubscriptionPlans;
import com.extractor.unraveldocs.subscription.enums.SubscriptionStatus;
import com.extractor.unraveldocs.subscription.model.SubscriptionPlan;
import com.extractor.unraveldocs.subscription.model.UserSubscription;
import com.extractor.unraveldocs.subscription.repository.SubscriptionPlanRepository;
import com.extractor.unraveldocs.subscription.repository.UserSubscriptionRepository;
import com.extractor.unraveldocs.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignSubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    //private final UserSubscriptionRepository userSubscriptionRepository;

    //@Transactional
    public UserSubscription assignDefaultSubscription(User user) {
        SubscriptionPlans planName = determinePlanNameForRole(user.getRole());

        SubscriptionPlan plan = planRepository.findByName(planName)
                .orElseThrow(() -> new NotFoundException("Default subscription plan '" + planName + "' not found. Please ensure plans are created by an admin."));

        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE.getStatusName());
        subscription.setHasUsedTrial(false);

        return subscription;
    }

    private SubscriptionPlans determinePlanNameForRole(Role role) {
        return switch (role) {
            case SUPER_ADMIN, ADMIN -> SubscriptionPlans.ENTERPRISE_YEARLY;
            case MODERATOR -> SubscriptionPlans.PREMIUM_YEARLY;
            default -> SubscriptionPlans.FREE;
        };
    }
}