-- Create verification_status table
CREATE TABLE verification_status (
    id VARCHAR(36) PRIMARY KEY,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(255),
    status VARCHAR(20) DEFAULT 'pending',
    email_verification_token_expiry TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_token_expiry TIMESTAMP
);

-- Create roles table
CREATE TABLE roles (
    id VARCHAR(36) PRIMARY KEY,
    role_name VARCHAR(20) NOT NULL
);

-- Create users table
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_login TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT FALSE NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE NOT NULL,
    verification_status_id VARCHAR(36),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (verification_status_id) REFERENCES verification_status(id)
);

-- Create join table for users and roles many-to-many relationship
CREATE TABLE users_roles (
    user_id VARCHAR(36) NOT NULL,
    role_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_verification_status_email_token ON verification_status(email_verification_token);
CREATE INDEX idx_verification_status_password_token ON verification_status(password_reset_token);
CREATE INDEX idx_roles_role_name ON roles(role_name);