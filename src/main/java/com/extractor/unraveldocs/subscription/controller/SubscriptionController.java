package com.extractor.unraveldocs.subscription.controller;

import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.extractor.unraveldocs.subscription.dto.response.SubscriptionPlansData;
import com.extractor.unraveldocs.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Create a new subscription plan",
            description = "Allows an admin to create a new subscription plan.",
            responses = {
                   @ApiResponse(responseCode = "201", description = "Subscription plan created successfully",
                   content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = SubscriptionPlansData.class))),
                   @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized"),
                   @ApiResponse(responseCode = "500", description = "Internal Server Error - Failed to create subscription plan")
            }
    )
    @PostMapping("plans")
    public ResponseEntity<UnravelDocsDataResponse<SubscriptionPlansData>> createSubscriptionPlan(CreateSubscriptionPlanRequest request) {
        UnravelDocsDataResponse<SubscriptionPlansData> createdPlan = subscriptionService.createSubscriptionPlan(request);

        return new ResponseEntity<>(createdPlan, HttpStatus.CREATED);
    }
}
