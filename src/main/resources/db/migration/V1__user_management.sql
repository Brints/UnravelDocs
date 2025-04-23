-- Create user_verification table
CREATE TABLE user_verification (
    id VARCHAR(36) PRIMARY KEY,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    email_verification_token_expiry TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    image_url VARCHAR(512),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    user_verification_id VARCHAR(36),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_verification_id) REFERENCES user_verification(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_user_verification_email_token ON user_verification(email_verification_token);
CREATE INDEX idx_user_verification_password_token ON user_verification(password_reset_token);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_users_verified ON users(is_verified);
CREATE INDEX idx_users_active_verified ON users(is_active, is_verified);
CREATE INDEX idx_users_role_active ON users(role, is_active);