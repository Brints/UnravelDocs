package com.extractor.unraveldocs.subscription.repository;

import com.extractor.unraveldocs.subscription.model.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, String> {
}