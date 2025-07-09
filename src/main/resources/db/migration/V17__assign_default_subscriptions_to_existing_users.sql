-- V17__assign_default_subscriptions_to_existing_users.sql
-- This script assigns a default subscription to all existing users who do not already have one.
-- It uses the plans created in the V16 seed script.

-- Assign 'ENTERPRISE' plan to SUPER_ADMIN and ADMIN users
INSERT INTO user_subscriptions (id, user_id, plan_id, status, has_used_trial, created_at, updated_at)
SELECT
    gen_random_uuid(),
    u.id,
    (SELECT id FROM subscription_plans WHERE name = 'ENTERPRISE_YEARLY'),
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.role IN ('SUPER_ADMIN', 'ADMIN') AND NOT EXISTS (
    SELECT 1 FROM user_subscriptions us WHERE us.user_id = u.id
);

-- Assign 'PREMIUM' plan to MODERATOR users
INSERT INTO user_subscriptions (id, user_id, plan_id, status, has_used_trial, created_at, updated_at)
SELECT
    gen_random_uuid(),
    u.id,
    (SELECT id FROM subscription_plans WHERE name = 'PREMIUM_YEARLY'),
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.role = 'MODERATOR' AND NOT EXISTS (
    SELECT 1 FROM user_subscriptions us WHERE us.user_id = u.id
);

-- Assign 'FREE' plan to all other users (e.g., USER role)
INSERT INTO user_subscriptions (id, user_id, plan_id, status, has_used_trial, created_at, updated_at)
SELECT
    gen_random_uuid(),
    u.id,
    (SELECT id FROM subscription_plans WHERE name = 'FREE'),
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM
    users u
WHERE
    u.role NOT IN ('SUPER_ADMIN', 'ADMIN', 'MODERATOR') AND NOT EXISTS (
    SELECT 1 FROM user_subscriptions us WHERE us.user_id = u.id
);