package com.extractor.unraveldocs.subscription.repository;

import com.extractor.unraveldocs.subscription.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, String> {
}