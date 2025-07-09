-- V16__seed_subscription_plans.sql
-- This script seeds the subscription_plans table with initial data.
-- Note: The UUIDs are hardcoded to ensure consistency across environments.

INSERT INTO subscription_plans (id, name, price, currency, billing_interval_unit, billing_interval_value, document_upload_limit, ocr_page_limit, is_active, created_at, updated_at)
VALUES
    -- Free Plan
    ('f1b4e6a0-1b1a-4b1a-8b1a-1b1a1b1a1b1a', 'FREE', 0.00, 'USD', 'month', 1, 50, 100, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Basic Plan
    ('f1b4e6a0-2c2c-4c2c-8c2c-2c2c2c2c2c2c', 'BASIC_MONTHLY', 10.00, 'USD', 'month', 1, 500, 1000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('f1b4e6a0-2c2c-4c2c-8c2c-2c2c2c2c2c3', 'BASIC_YEARLY', 100.00, 'USD', 'year', 1, 500, 1000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Pro Plan
    ('f1b4e6a0-3c3c-4c3c-8c3c-3c3c3c3c3c3c', 'PREMIUM_MONTHLY', 25.00, 'USD', 'month', 1, 2000, 5000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('f1b4e6a0-3c3c-4c3c-8c3c-3c3c3c3c3c4', 'PREMIUM_YEARLY', 250.00, 'USD', 'year', 1, 2000, 5000, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Enterprise Plan
    ('f1b4e6a0-4d4d-4d4d-8d4d-4d4d4d4d4d4d', 'ENTERPRISE_MONTHLY', 75.00, 'USD', 'month', 1, -1, -1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('f1b4e6a0-4d4d-4d4d-8d4d-4d4d4d4d4d5', 'ENTERPRISE_YEARLY', 750.00, 'USD', 'year', 1, -1, -1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);