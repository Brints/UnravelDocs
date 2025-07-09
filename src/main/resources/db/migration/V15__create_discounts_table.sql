-- Create discounts table for promotional offers on subscription plans
CREATE TABLE discounts (
    id VARCHAR(36) PRIMARY KEY,
    plan_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    discount_percent DECIMAL(5, 2) NOT NULL,
    start_date TIMESTAMP WITH TIME ZONE NOT NULL,
    valid_until TIMESTAMP WITH TIME ZONE NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES subscription_plans(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_discounts_plan_id ON discounts(plan_id);
CREATE INDEX idx_discounts_active_dates ON discounts(is_active, start_date, valid_until);